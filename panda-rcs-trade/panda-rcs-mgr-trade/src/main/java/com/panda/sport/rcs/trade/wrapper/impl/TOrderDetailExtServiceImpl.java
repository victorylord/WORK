package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.TOrderDetailExtMapper;
import com.panda.sport.rcs.mapper.TOrderMapper;
import com.panda.sport.rcs.pojo.TOrderDetailExt;
import com.panda.sport.rcs.pojo.vo.OrderTakingVo;
import com.panda.sport.rcs.repository.TOrderDetailExtRepository;
import com.panda.sport.rcs.trade.enums.HandleStatusEnum;
import com.panda.sport.rcs.utils.TOrderDetailExtUtils;
import com.panda.sport.rcs.trade.wrapper.LogFormatService;
import com.panda.sport.rcs.trade.wrapper.TOrderDetailExtService;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.TOrderDetailExtDO;
import com.panda.sport.rcs.wrapper.OrderAcceptRejectService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.task.wrapper.impl
 * @Description :  订单明细扩展
 * @Date: 2020-01-31 11:46
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class TOrderDetailExtServiceImpl extends ServiceImpl<TOrderDetailExtMapper, TOrderDetailExt> implements TOrderDetailExtService {
    @Autowired
    private TOrderDetailExtMapper dao;
    @Autowired
    private OrderAcceptRejectService orderAcceptRejectService;
    @Autowired
    private TOrderMapper orderMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private LogFormatService logFormatService;
    @Autowired
    private TOrderDetailExtRepository tOrderDetailExtRepository;
    @Autowired
    private TOrderDetailExtUtils tOrderDetailExtUtils;
    /**
     * @return int
     * @Description 批量处理订单状态
     * @Param [orderStatus, ids]
     * @Author toney
     * @Date 10:15 2020/2/1
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse orderTakingBatch(OrderTakingVo vo) {
        int num = 0;
        String state = vo.getState();
        if (HandleStatusEnum.FOUR.getCode().equalsIgnoreCase(state)) {
            //手动接单
            if(tOrderDetailExtUtils.isSaveToMongo()) {
                num = tOrderDetailExtRepository.orderTakingBatch(Integer.valueOf(state), vo.getIds());
            }else {
                num = dao.orderTakingBatch(state, vo.getIds());
            }
        } else if (HandleStatusEnum.FIVE.getCode().equalsIgnoreCase(state)) {
            // 手动拒单
            List<String> list;
            if(tOrderDetailExtUtils.isSaveToMongo()){
                list = tOrderDetailExtRepository.queryOrderNoByBetNo(vo.getIds());
                if (CollectionUtils.isNotEmpty(list)) {
                    num = tOrderDetailExtRepository.orderTakingBatch(Integer.valueOf(state), vo.getIds());
                    orderMapper.denialOrderDetailByIds(list, vo.getUserName()+"-手动拒单" );
                }
            }else{
                list = dao.queryOrderNoByBetNo(vo.getIds());
                if (CollectionUtils.isNotEmpty(list)) {
                    num = dao.orderTakingBatch(state, vo.getIds());
                    orderMapper.denialOrderDetailByIds(list, vo.getUserName()+"-手动拒单" );
                }
            }
        } else if (HandleStatusEnum.THREE.getCode().equals(state)) {
            //取消
            orderAcceptRejectService.sendMessage(vo.getIds());
        }else if (HandleStatusEnum.EIGHT.getCode().equals(state)) {
            //暂停接单
            if(tOrderDetailExtUtils.isSaveToMongo()){
                num = tOrderDetailExtRepository.pauseOrderTakingBatch(vo, Integer.valueOf(vo.getState()));
            }else {
                num = dao.pauseOrderTakingBatch(vo);
            }
        }else if (HandleStatusEnum.NINE.getCode().equals(state)) {
            //暂停拒单
            List<String> orderNos;
            if(tOrderDetailExtUtils.isSaveToMongo()){
                orderNos = tOrderDetailExtRepository.queryOrderNo(vo);
                if (CollectionUtils.isNotEmpty(orderNos)) {
                    num = tOrderDetailExtRepository.pauseOrderTakingBatch(vo, Integer.valueOf(vo.getState()));
                    orderMapper.denialOrderDetailByIds(orderNos,  vo.getUserName()+"-暂停拒单");
                }
            }else {
                orderNos = dao.queryOrderNo(vo);
                if (CollectionUtils.isNotEmpty(orderNos)) {
                    num = dao.pauseOrderTakingBatch(vo);
                    orderMapper.denialOrderDetailByIds(orderNos,  vo.getUserName()+"-暂停拒单");
                }
            }
        }else if(HandleStatusEnum.TEN.getCode().equals(state)){
            //忽略暂停
            if(tOrderDetailExtUtils.isSaveToMongo()){
                num = tOrderDetailExtRepository.pauseOrderTakingBatch(vo, Integer.parseInt(HandleStatusEnum.TEN.getCode()));
            }else {
                List<TOrderDetailExt> tOrderDetailExts = dao.queryExtByBetNo(vo);
                if (CollectionUtils.isNotEmpty(tOrderDetailExts)) {
                    for (TOrderDetailExt ext : tOrderDetailExts) {
                        ext.setOrderStatus(Integer.parseInt(HandleStatusEnum.TEN.getCode()));
                    }
                    num = dao.updateBatch(tOrderDetailExts);
                }
            }

            /*List<OrderNoPlaceNum> orderNoPlaceNums = dao.queryOrderNoPlaceNum(vo);
            if(!CollectionUtils.isEmpty(orderNoPlaceNums)){
                orderNoPlaceNums.stream().forEach(ve->{
                    String statusConfigKey = RedisKey.getMarketPlaceStatusConfigKey(ve.getMatchId(), ve.getPlayId().longValue());
                    String value = redisClient.hGet(statusConfigKey, String.valueOf(ve.getPlaceNum()));
                    if(StringUtils.isNotBlank(value)){
                        ve.setPlaceNumStatus(Integer.parseInt(value));
                    }else {
                        ve.setPlaceNumStatus(MarketStatusEnum.OPEN.getState());
                    }
                });
                List<String> openBetNos = orderNoPlaceNums.stream().filter(fi -> null != fi.getPlaceNumStatus() && MarketStatusEnum.OPEN.getState() == fi.getPlaceNumStatus())
                        .map(map -> map.getBetNo()).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(openBetNos)){
                    num = dao.orderTakingBatch(HandleStatusEnum.EIGHT.getCode(), openBetNos);
                }
                List<String> notOpenBetNos = orderNoPlaceNums.stream().filter(fi -> null != fi.getPlaceNumStatus() && MarketStatusEnum.OPEN.getState() != fi.getPlaceNumStatus())
                        .map(map -> map.getBetNo()).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(notOpenBetNos)){
                    num = dao.orderTakingBatch(HandleStatusEnum.NINE.getCode(), notOpenBetNos);
                    List<String> orderNos = orderNoPlaceNums.stream().filter(fi -> null != fi.getPlaceNumStatus() && MarketStatusEnum.OPEN.getState() != fi.getPlaceNumStatus())
                            .map(map -> map.getOrderNo()).collect(Collectors.toList());
                    if(!CollectionUtils.isEmpty(orderNos)){
                        orderMapper.denialOrderDetailByIds(orderNos,  vo.getUserName()+"-暂停忽略拒单");
                    }
                }
            }*/
        }

        if (num == 0 && !HandleStatusEnum.THREE.getCode().equals(state)&&!CollectionUtils.isEmpty(vo.getIds())) {
            if(Arrays.asList("8","9","10").contains(state)){
                return HttpResponse.error(202,"未查询到订单");
            }
            List<String> doneBetNos;
            if(tOrderDetailExtUtils.isSaveToMongo()){
                doneBetNos = getDoneBetNosMongo(vo, state);
            } else {
                doneBetNos = getDoneBetNos(vo, state);
            }
            if (!CollectionUtils.isEmpty(doneBetNos)) {
                return HttpResponse.error(201, JsonFormatUtils.toJson(doneBetNos) + "注单已处理，请核对数据！");
            }else {
                return HttpResponse.error(202,"未查询到订单");
            }
        }
        //记录操作日志
        logFormatService.orderTakingBatchLog(vo);
        return HttpResponse.success();
    }

    @Deprecated
    private List<String> getDoneBetNos(OrderTakingVo vo, String state) {
        QueryWrapper<TOrderDetailExt> templateQueryWrapper = new QueryWrapper();
        if(HandleStatusEnum.FOUR.getCode().equals(state)
                || HandleStatusEnum.FIVE.getCode().equals(state)){
            templateQueryWrapper.lambda().in(TOrderDetailExt::getBetNo, vo.getIds());
        }else {
            templateQueryWrapper.lambda().notIn(TOrderDetailExt::getBetNo, vo.getIds());
        }
        List<TOrderDetailExt> tOrderDetailExts = dao.selectList(templateQueryWrapper);
        List<String> doneBetNos = null;
        if (CollectionUtils.isNotEmpty(tOrderDetailExts)) {
            doneBetNos = tOrderDetailExts.stream().filter(fi -> !Arrays.asList(0,-1).contains(fi.getOrderStatus())).map(TOrderDetailExt::getBetNo).collect(Collectors.toList());
        }
        return doneBetNos;
    }

    public List<String> getDoneBetNosMongo(OrderTakingVo vo, String state) {
        Criteria criteria = new Criteria();
        if(HandleStatusEnum.FOUR.getCode().equals(state)
                || HandleStatusEnum.FIVE.getCode().equals(state)){
            criteria.and("betNo").in(vo.getIds());
        }else {
            criteria.and("betNo").nin(vo.getIds());
        }
        List<TOrderDetailExtDO> tOrderDetailExts = tOrderDetailExtRepository.searchByCriteria(criteria);
        List<String> doneBetNos = null;
        if (CollectionUtils.isNotEmpty(tOrderDetailExts)) {
            doneBetNos = tOrderDetailExts.stream().filter(fi -> !Arrays.asList(0,-1).contains(fi.getOrderStatus())).map(TOrderDetailExtDO::getBetNo).collect(Collectors.toList());
        }
        return doneBetNos;
    }

}
