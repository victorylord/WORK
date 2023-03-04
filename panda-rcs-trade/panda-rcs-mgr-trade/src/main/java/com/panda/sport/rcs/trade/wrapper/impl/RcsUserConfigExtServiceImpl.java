package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.mapper.RcsUserConfigExtMapper;
import com.panda.sport.rcs.mapper.TUserMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.vo.LogData;
import com.panda.sport.rcs.trade.enums.SpecialBettingLimitTypeEnum;
import com.panda.sport.rcs.trade.enums.UserLogTypeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.ThreadUtil;
import com.panda.sport.rcs.trade.vo.RcsMerchantUserTagMarketLevelStatusReqVo;
import com.panda.sport.rcs.trade.vo.RcsUserConfigExtReqVo;
import com.panda.sport.rcs.trade.wrapper.IRcsUserConfigExtService;
import com.panda.sport.rcs.trade.wrapper.IRcsUserConfigNewService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sports.api.vo.ShortSysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户特殊配置扩展
 *
 * @description:
 * @author: magic
 * @create: 2022-05-14 18:15
 **/
@Slf4j
@Service
public class RcsUserConfigExtServiceImpl extends ServiceImpl<RcsUserConfigExtMapper, RcsUserConfigExt> implements IRcsUserConfigExtService {

    @Autowired
    RcsOperationLogMapper rcsOperationLogMapper;
    @Autowired
    RcsTradingAssignmentService rcsTradingAssignmentService;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    TUserMapper tUserMapper;
    @Autowired
    ProducerSendMessageUtils sendMessage;

    @Autowired
    IRcsUserConfigNewService rcsUserConfigNewService;
    /**
     * 限额缓存
     */
    private String hkey = "risk:trade:rcs_user_special_bet_limit_config:%s";
    /**
     * 类型
     */
    private String Hkey1 = "type";

    /**
     * 给业务发生mq
     *
     * @param list
     */
    private void sendTagMarketLevelStatus(List<RcsUserConfigExt> list) {
        if (!list.isEmpty()) {
            List<RcsUserConfigExtReqVo> voList = list.stream().map(e -> {
                RcsUserConfigExtReqVo rcsUserConfigExtReqVo = new RcsUserConfigExtReqVo();
                BeanUtils.copyProperties(e, rcsUserConfigExtReqVo);
                return rcsUserConfigExtReqVo;
            }).collect(Collectors.toList());
            //先用这个topic名字，如果后续有更多的配置加进来  再改topic名称或继续沿用这个名称
            sendMessage.sendMessage("rcs_user_tag_market_level_status", voList);
        }
    }

    @Override
    @Transactional
    public void saveTagMarketLevelStatus(RcsUserConfigExtReqVo rcsUserConfigExtReqVo, int traderId) {
        RcsUserConfigExt rcsUserConfigExt = new RcsUserConfigExt();
        BeanUtils.copyProperties(rcsUserConfigExtReqVo, rcsUserConfigExt);
        RcsUserConfigExt o = baseMapper.selectOne(new LambdaQueryWrapper<RcsUserConfigExt>().eq(RcsUserConfigExt::getUserId, rcsUserConfigExtReqVo.getUserId()));

        RcsOperationLog rcsOperationLog = new RcsOperationLog();
        List<LogData> logDataList = new ArrayList<>();
        LogData logData = new LogData();
        logData.setType(UserLogTypeEnum.DYNAMIC_GROUP.getValue());
        logData.setName(UserLogTypeEnum.DYNAMIC_GROUP.getType());
        logData.setData(String.valueOf(rcsUserConfigExtReqVo.getTagMarketLevelStatus()));

        if (o != null) {
            rcsUserConfigExt.setId(o.getId());
            rcsUserConfigExt.setUpdateTime(new Date());
            rcsUserConfigExt.setMerchantCode(o.getMerchantCode());
            logData.setOldData(String.valueOf(o.getTagMarketLevelStatus()));
        } else {
            TUser tUser = tUserMapper.selectByUserId(rcsUserConfigExtReqVo.getUserId());
            rcsUserConfigExt.setMerchantCode(tUser.getMerchantCode());
            logData.setOldData("1");//默认为开
        }
        logDataList.add(logData);

        saveOrUpdate(rcsUserConfigExt);
        ShortSysUserVO traderData = rcsTradingAssignmentService.getShortSysUserById(traderId);
        if (traderData == null) {
            traderData = new ShortSysUserVO();
        }

        LogData logDataUser = new LogData();
        logDataUser.setType(UserLogTypeEnum.TRADER.getValue());
        logDataUser.setName("操作人");
        logDataUser.setData(traderData.getUserCode());
        logDataList.add(logDataUser);

        rcsOperationLog.setHandleCode("user_config_history");
        rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigExtReqVo.getUserId()));
        rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));
        rcsOperationLog.setShowContent("23");
        rcsOperationLogMapper.saveBatchRcsOperationLog(Arrays.asList(rcsOperationLog));

        //发送配置到业务
        sendTagMarketLevelStatus(Arrays.asList(rcsUserConfigExt));
    }

    @Override
    @Transactional
    public int batchSaveTagMarketLevelStatus(RcsMerchantUserTagMarketLevelStatusReqVo rcsMerchantUserTagMarketLevelStatusReqVo, int traderId) {
        //risk:trade:rcs_user_special_bet_limit_config:183048588675719168
        //1 先查询出所有设置过特殊限额的并且数值匹配的用户
        //2 去缓存确认特殊限额类型     因为查询数据库数据数据有问题，没办法直接通过sql查询出具体类型 所以需要去缓存确认
        List<Long> userIds = tUserMapper.findByPercentageLimit(rcsMerchantUserTagMarketLevelStatusReqVo.getPercentageLimit(), rcsMerchantUserTagMarketLevelStatusReqVo.getMerchantCode());
        if (userIds.isEmpty()) {
            return 0;
        }
        List<String> types = ThreadUtil.executeFutures(userIds, (e) -> {
            String type = redisUtils.hget(String.format(hkey, e), Hkey1);
            if (StringUtils.isBlank(type)) {
                //因为缓存问题，导致缓存不存在，需要重新去配置表查询确认类型，多线程查询，后续有性能问题，再抽出来批量查询
                RcsUserConfigNew rcsUserConfigNew = rcsUserConfigNewService.getOne(new LambdaQueryWrapper<RcsUserConfigNew>().select(RcsUserConfigNew::getSpecialBettingLimit).eq(RcsUserConfigNew::getUserId, e).last(" limit 1"));
                if (rcsUserConfigNew != null && rcsUserConfigNew.getSpecialBettingLimit() != null) {
                    return rcsUserConfigNew.getSpecialBettingLimit().toString();
                }
            }
            return type;
        });

        List<RcsUserConfigExt> list = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            String typeStr = types.get(i);
            Long userId = userIds.get(i);
            if (StringUtils.isBlank(typeStr)) {
                log.error("用户：{}的特殊限额类型没有找到", userId);
                continue;
            }
            Integer type = Integer.valueOf(typeStr);
            //确认类型为2 特殊百分比限额
            if (SpecialBettingLimitTypeEnum.PERCENTAGE_LIMIT.getType().compareTo(type) != 0) {
                continue;
            }
            RcsUserConfigExt rcsUserConfigExt = new RcsUserConfigExt();
            rcsUserConfigExt.setUserId(userId);
            rcsUserConfigExt.setMerchantCode(rcsMerchantUserTagMarketLevelStatusReqVo.getMerchantCode());
            rcsUserConfigExt.setTagMarketLevelStatus(rcsMerchantUserTagMarketLevelStatusReqVo.getTagMarketLevelStatus());
            list.add(rcsUserConfigExt);
        }
        log.info("::{}::批量修改商户下用户赔率分组动态风控开关：查询到商户:{}下匹配限额:{} 的用户数量:{}个", CommonUtil.getRequestId(), rcsMerchantUserTagMarketLevelStatusReqVo.getMerchantCode(), rcsMerchantUserTagMarketLevelStatusReqVo.getPercentageLimit(), list.size());

        if (list.isEmpty()) {
            return 0;
        }
        //因为要old数据历史入日志，所以需要查一次
        List<RcsUserConfigExt> oldList = baseMapper.selectList(new LambdaQueryWrapper<RcsUserConfigExt>().in(RcsUserConfigExt::getUserId, list.stream().map(RcsUserConfigExt::getUserId).collect(Collectors.toList())));

        Map<Long, RcsUserConfigExt> oldsMap = oldList.stream().collect(Collectors.toMap(RcsUserConfigExt::getUserId, e -> e));
        Date now = new Date();
        //如果存在历史数据的设置id做update
        list.forEach(e -> {
            RcsUserConfigExt old = oldsMap.get(e.getUserId());
            if (old != null) {
                e.setId(old.getId());
                e.setUpdateTime(now);
            }
        });
        List<RcsUserConfigExt> insertList = list.stream().filter(e -> e.getId() == null).collect(Collectors.toList());
        List<RcsUserConfigExt> updateList = list.stream().filter(e -> e.getId() != null).collect(Collectors.toList());
        if (!updateList.isEmpty()) {
            updateBatchById(updateList);
        }
        if (!insertList.isEmpty()) {
            saveBatch(insertList);
        }
        ShortSysUserVO traderData = rcsTradingAssignmentService.getShortSysUserById(traderId);

        //日志处理
        List<RcsOperationLog> logList = list.stream().map(e -> {
            List<LogData> logDataList = new ArrayList<>();
            LogData logData = new LogData();
            logData.setType(UserLogTypeEnum.DYNAMIC_GROUP.getValue());
            logData.setName(UserLogTypeEnum.DYNAMIC_GROUP.getType());
            logData.setData(String.valueOf(e.getTagMarketLevelStatus()));
            RcsUserConfigExt old = oldsMap.get(e.getUserId());
            if (old != null) {
                logData.setOldData(String.valueOf(old.getTagMarketLevelStatus()));
            }else{
                logData.setOldData("1");//默认为开
            }
            logDataList.add(logData);

            LogData logDataUser = new LogData();
            logDataUser.setType(UserLogTypeEnum.TRADER.getValue());
            logDataUser.setName("操作人");
            logDataUser.setData(traderData.getUserCode());
            logDataList.add(logDataUser);

            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(e.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));
            rcsOperationLog.setShowContent("24");
            return rcsOperationLog;
        }).collect(Collectors.toList());
        rcsOperationLogMapper.saveBatchRcsOperationLog(logList);

        //发送配置到业务
        sendTagMarketLevelStatus(list);
        return list.size();
    }
}
