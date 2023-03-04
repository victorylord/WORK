package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsLabelLimitConfigMapper;
import com.panda.sport.rcs.mapper.TTagMarketLevelMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsLabelSportVolumePercentage;
import com.panda.sport.rcs.pojo.TTagMarket;
import com.panda.sport.rcs.pojo.TTagMarketLevel;
import com.panda.sport.rcs.pojo.TTagMarketReqVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsLabelSportVolumePercentageService;
import com.panda.sport.rcs.trade.wrapper.RcsLabelLimitConfigService;
import com.panda.sport.rcs.trade.wrapper.TTagMarketService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsLabelLimitConfigDataVo;
import com.panda.sport.rcs.vo.RcsLabelLimitConfigVo;
import com.panda.sport.rcs.vo.RcsLabelSportVolumePercentageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: xindaima
 * @description:  标签限额配置
 * @author: kimi
 * @create: 2021-02-04 12:16
 **/
@RestController
@Slf4j
@RequestMapping("/rcsLabelLimitConfig")
public class RcsLabelLimitConfigController {
    @Autowired
    private RcsLabelLimitConfigMapper rcsLabelLimitConfigMapper;
    @Autowired
    private RcsLabelLimitConfigService rcsLabelLimitConfigService;

    @Autowired
    private TTagMarketLevelMapper marketLevelMapper;

    @Autowired
    private TTagMarketService marketService;

    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Autowired
    IRcsLabelSportVolumePercentageService rcsLabelSportVolumePercentageService;

    /**
     * 获取标签限额数据
     * @return
     */
    @RequestMapping(value = "/getList",method = RequestMethod.GET)
    public HttpResponse<RcsLabelLimitConfigDataVo>  getList(@RequestParam(required = false)  Integer pageNum, @RequestParam(required = false) Integer pageSize){
        try {
            if (pageNum==null){
                pageNum=1;
            }
            if (pageSize==null){
                pageSize=1000;
            }
            RcsLabelLimitConfigDataVo rcsLabelLimitConfigDataVo=new RcsLabelLimitConfigDataVo();
            IPage<RcsLabelLimitConfigVo> rcsLabelLimitConfigVoIPage=new Page<>(pageNum,pageSize);
            IPage<RcsLabelLimitConfigVo> iPage = rcsLabelLimitConfigMapper.selectRcsLabelLimitConfig(rcsLabelLimitConfigVoIPage);
            List<RcsLabelLimitConfigVo> rcsLabelLimitConfigVos = iPage.getRecords();
            HashMap<Integer, RcsLabelLimitConfigVo> hashMap = new HashMap<>();
            for (RcsLabelLimitConfigVo rcsLabelLimitConfigVo : rcsLabelLimitConfigVos) {
                RcsLabelLimitConfigVo rcsLabelLimitConfigVo1 = hashMap.get(rcsLabelLimitConfigVo.getTagId());
                if (rcsLabelLimitConfigVo1 == null) {
                    rcsLabelLimitConfigVo1 = rcsLabelLimitConfigVo;
                    hashMap.put(rcsLabelLimitConfigVo1.getTagId(), rcsLabelLimitConfigVo1);
                    if (rcsLabelLimitConfigVo1.getVolumePercentage()==null){
                        rcsLabelLimitConfigVo1.setVolumePercentage(new BigDecimal(100));
                    }else {
                        rcsLabelLimitConfigVo1.setVolumePercentage(rcsLabelLimitConfigVo1.getVolumePercentage().multiply(new BigDecimal(100)));
                    }
                }
                List<Integer> sportIdList = rcsLabelLimitConfigVo1.getSportIdList();
                if (rcsLabelLimitConfigVo1.getSportId()!=null){
                    if (sportIdList==null){
                        sportIdList=new ArrayList<>();
                        rcsLabelLimitConfigVo1.setSportIdList(sportIdList);
                    }
                    sportIdList.add(rcsLabelLimitConfigVo.getSportId());
                }
                rcsLabelLimitConfigVo1.setSportIdList(sportIdList);
            }
            rcsLabelLimitConfigDataVo.setRcsLabelLimitConfigVoList(hashMap.values());
            rcsLabelLimitConfigDataVo.setTotal(iPage.getTotal());
            rcsLabelLimitConfigDataVo.setGetCurrent(iPage.getCurrent());

            //返回数据前 新增逻辑 标签百分比货量vo
            for (RcsLabelLimitConfigVo rcsLabelLimitConfigVo : rcsLabelLimitConfigDataVo.getRcsLabelLimitConfigVoList()) {
                //查询出标签对应的所有货量配置
                LambdaQueryWrapper<RcsLabelSportVolumePercentage> svWrapper = new LambdaQueryWrapper<>();
                svWrapper.eq(RcsLabelSportVolumePercentage::getTagId, rcsLabelLimitConfigVo.getTagId());
                List<RcsLabelSportVolumePercentage> volumePercentageList = rcsLabelSportVolumePercentageService.list(svWrapper);
                //所有配置返回到对应标签数据体
                List<RcsLabelSportVolumePercentageVo> voList = new ArrayList<>();
                for (RcsLabelSportVolumePercentage rcsLabelSportVolumePercentage : volumePercentageList) {
                    RcsLabelSportVolumePercentageVo volumePercentageVo = new RcsLabelSportVolumePercentageVo();
                    BeanUtils.copyProperties(rcsLabelSportVolumePercentage,volumePercentageVo);
                    voList.add(volumePercentageVo);
                }
                //设置数据体返回
                rcsLabelLimitConfigVo.setSportVolumePercentageList(voList);
            }

            return HttpResponse.success(rcsLabelLimitConfigDataVo);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器故障");
        }
    }

    /**
     * 更新标签限额数据
     * @param rcsLabelLimitConfigVoList
     * @return
     */
    @RequestMapping(value = "/update",method = RequestMethod.POST)
    public HttpResponse update(@RequestBody List<RcsLabelLimitConfigVo> rcsLabelLimitConfigVoList) {
        try {
            Integer userId = TradeUserUtils.getUserId();
            log.info("::{}::更新标签限额数据,操盘手:{}",CommonUtil.getRequestId(), userId);
            return rcsLabelLimitConfigService.updateRcsLabelLimitConfigVo(rcsLabelLimitConfigVoList, userId);
        }catch (RcsServiceException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器故障");
        }
    }
    /**
     * 获取全部行情等級
     * @return
     */
    @RequestMapping(value = "/getTagMarketLevelList", method = RequestMethod.GET)
    public HttpResponse<List<TTagMarketLevel>>  getTagMarketLevelList(){
        try {
            QueryWrapper<TTagMarketLevel> wrapper = new QueryWrapper();
            List<TTagMarketLevel> list = marketLevelMapper.selectList(wrapper);
            return HttpResponse.success(list);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器故障");
        }
    }

    /**
     * 获取全部行情等級
     * @return
     */
    @RequestMapping(value = "/getTagMarketList", method = RequestMethod.GET)
    public HttpResponse<List<TTagMarketReqVo>> getTagMarketList(){
        try {
            List<TTagMarketReqVo> tagMarketList = marketService.getTagMarketList();
            return HttpResponse.success(tagMarketList);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器故障");
        }
    }

    /**
     * 更新标签限额数据
     * @return
     */
    @RequestMapping(value = "/updateTagMarket",method = RequestMethod.POST)
    public HttpResponse updateTagMarket(@RequestBody List<TTagMarket> tagMarketList) {
        try {
            for (TTagMarket tTagMarket : tagMarketList) {
                marketService.updateById(tTagMarket);
            }

            //编辑时，批量把最新的标签信息发送给业务
            List<TTagMarketReqVo> tagMarketReqVoList = marketService.getTagMarketList();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("type", 2);
            map1.put("entity", tagMarketReqVoList);
            sendMessage.sendMessage("USER_FINANCE_TAG_CHANGE_TOPIC", map1);
            log.info("::{}::{}消息发送成功",CommonUtil.getRequestId(), "USER_FINANCE_TAG_CHANGE_TOPIC");
            return HttpResponse.success();
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器故障");
        }
    }
}
