package com.panda.sport.rcs.trade.mq.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.mapper.TUserBetRateMapper;
import com.panda.sport.rcs.pojo.RcsOperationLog;
import com.panda.sport.rcs.pojo.TUserBetRate;
import com.panda.sport.rcs.pojo.vo.LogData;
import com.panda.sport.rcs.trade.enums.UserLogTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 根据玩家组id修改所有所属玩家的货量百分比配置
 * @Author : Kir
 * @Date : 2021-08-20 18:31
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "USER_GROUP_BET_RATE_TOPIC",
        consumerGroup = "RCS_TRADE_USER_GROUP_BET_RATE_TOPIC",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class UpdateUserBetRateConsumer extends RcsConsumer<JSONObject> {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 限额缓存
     */
    private String hkey = "risk:trade:rcs_user_sport_type_bet_limit_config:%s";

    @Autowired
    private TUserBetRateMapper userBetRateMapper;

    @Autowired
    private RcsOperationLogMapper rcsOperationLogMapper;

    @Override
    protected String getTopic() {
        return "USER_GROUP_BET_RATE_TOPIC";
    }

    @Override
    public Boolean handleMs(JSONObject msg) {
        try {
            log.info("::{}::USER_GROUP_BET_RATE_TOPIC:{}",CommonUtil.getRequestId(),JSONObject.toJSONString(msg));
            if (ObjectUtil.isNotNull(msg) && ObjectUtil.isNotNull(msg.getJSONArray("userList"))) {
                //获取所有需要修改的用户
                List<Long> userList = JSONObject.parseArray(msg.getJSONArray("userList").toString(), Long.class);

                //修改人
                String modifyUser = msg.getString("modifyUser");

                //获取配置数据
                List<TUserBetRate> betRateConfig = JSONObject.parseArray(msg.getJSONObject("betRateConfig").getJSONArray("userGroupBetRateList").toString(), TUserBetRate.class);

                log.info("USER_GROUP_BET_RATE_TOPIC用户列表为{}", JSONObject.toJSONString(userList));
                log.info("USER_GROUP_BET_RATE_TOPIC配置列表为{}", JSONObject.toJSONString(betRateConfig));

                if(CollectionUtils.isEmpty(userList)){
                    log.info("USER_GROUP_BET_RATE_TOPIC用户列表为空,退出任务");
                    return false;
                }

                //获取数据库数据
                LambdaQueryWrapper<TUserBetRate> oldQueryWrapper = new LambdaQueryWrapper<>();
                oldQueryWrapper.in(TUserBetRate::getUserId, userList);
                List<TUserBetRate> oldUserBetRateList = userBetRateMapper.selectList(oldQueryWrapper);
                Map<Long, List<TUserBetRate>> oldUserMap = new HashMap<>();
                if(!CollectionUtils.isEmpty(oldUserBetRateList)){
                    //处理有修改的用户，过滤掉没有修改的用户
                    List<Long> newList = new ArrayList<>();

                    //老数据分组(用于日志)
                    oldUserMap = oldUserBetRateList.stream().collect(Collectors.groupingBy(TUserBetRate::getUserId));

                    //如果传过来的用户不存在数据库中，则添加进需要变动的用户之中去
                    for (Long userId : userList) {
                        if(oldUserMap.get(userId)==null){
                            newList.add(userId);
                        }
                    }

                    for (Map.Entry<Long, List<TUserBetRate>> entry : oldUserMap.entrySet()) {
                        userList.contains(entry.getKey());
                        if(userList.contains(entry.getKey())){
                            //判断值是否一样
                            List<TUserBetRate> oldData = entry.getValue();
                            Map<String, String> oldCollect = oldData.stream().collect(Collectors.toMap(e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2)), e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2))));
                            Map<String, String> collect = betRateConfig.stream().collect(Collectors.toMap(e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2)), e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2))));
                            if(CollectionUtils.isEmpty(collect)){
                                if(!CollectionUtils.isEmpty(oldCollect)){
                                    newList.add(entry.getKey());
                                }
                            }

                            List<String> collect1 = collect.keySet().stream().filter(e -> !oldCollect.keySet().contains(e)).collect(Collectors.toList());
                            if(!CollectionUtils.isEmpty(collect1)){
                                newList.add(entry.getKey());
                            }
                        }else{
                            newList.add(entry.getKey());
                        }
                    }
                    userList = newList;
                }

                log.info("USER_GROUP_BET_RATE_TOPIC有变动的用户有:{}", JSONObject.toJSONString(userList));
                if(!CollectionUtils.isEmpty(userList)){
                    log.info("清空所有用户的风控措施数据:{}", JSONObject.toJSONString(userList));
                    LambdaQueryWrapper<TUserBetRate> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(TUserBetRate::getUserId, userList);
                    userBetRateMapper.delete(wrapper);
                    log.info("USER_GROUP_BET_RATE_TOPIC清空所有用户风控措施数据{}", userList);

                    //清空所有用户的redis缓存
                    for (Long userId : userList) {
                        log.info("USER_GROUP_BET_RATE_TOPIC清空所有用户redis缓存数据{}", userList);
                        String format = String.format(hkey, String.valueOf(userId));
                        redisUtils.del(format);
                    }
                }

                if(!CollectionUtils.isEmpty(betRateConfig)){
                    log.info("USER_GROUP_BET_RATE_TOPIC传过来的风控措施配置共{}个,分别是:{}", betRateConfig.size(), JSONObject.toJSONString(betRateConfig));
                    //循环插入
                    for (TUserBetRate map : betRateConfig) {
                        Integer sportId = map.getSportId();
                        BigDecimal betRate = map.getBetRate();
                        for (Long userId : userList) {
                            TUserBetRate one = new TUserBetRate();
                            one.setBetRate(betRate);
                            one.setSportId(sportId);
                            one.setUserId(userId);
                            userBetRateMapper.insert(one);
                            //log.info("USER_GROUP_BET_RATE_TOPIC用户{}的风控措施数据新增成功", userId);

                            //修改玩家组对应的用户后，重新set各个用户的限额数据
                            String format = String.format(hkey, String.valueOf(userId));
                            redisUtils.hset(format, String.valueOf(sportId), String.valueOf(betRate));
                            //log.info("USER_GROUP_BET_RATE_TOPIC重置用户{}风控措施redis成功", userId);
                        }
                        //log.info("USER_GROUP_BET_RATE_TOPIC 用户的所有配置处理完成");
                    }
                }

                log.info("USER_GROUP_BET_RATE_TOPIC开始处理日志");
                if(!CollectionUtils.isEmpty(userList)){
                    //获取数据库数据
                    LambdaQueryWrapper<TUserBetRate> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.in(TUserBetRate::getUserId, userList);
                    List<TUserBetRate> userBetRateList = userBetRateMapper.selectList(queryWrapper);

                    //用户分组
                    Map<Long, List<TUserBetRate>> userMap = userBetRateList.stream().collect(Collectors.groupingBy(TUserBetRate::getUserId));

                    //日志数据组装
                    List<RcsOperationLog> rcsOperationLogList=new ArrayList<>();

                    //如果传过来的配置为null，则为所有有变更的用户加上日志
                    if(CollectionUtils.isEmpty(betRateConfig)){
                        for (Long userId : userList) {
                            List<LogData> logDataList=new ArrayList<>();
                            LogData logData = new LogData();
                            logData.setType(UserLogTypeEnum.BETRATE.getValue());
                            logData.setName("用户各赛种货量百分比");
                            logData.setOldData( oldUserMap.get(userId)==null ? "" : JSONObject.toJSONString(oldUserMap.get(userId)));
                            logData.setData(null);
                            logDataList.add(logData);
                            //操作人
                            LogData logData1=new LogData();
                            logData1.setType(UserLogTypeEnum.TRADER.getValue());
                            logData1.setName("操作人");
                            logData1.setData(modifyUser);
                            logDataList.add(logData1);
                            RcsOperationLog rcsOperationLog = new RcsOperationLog();
                            rcsOperationLog.setHandleCode("user_config_history");
                            rcsOperationLog.setHanlerId(String.valueOf(userId));
                            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));
                            rcsOperationLog.setShowContent("16");
                            rcsOperationLogList.add(rcsOperationLog);
                        }
                    }

                    for (Map.Entry<Long, List<TUserBetRate>> entry : userMap.entrySet()) {
                        List<LogData> logDataList=new ArrayList<>();
                        LogData logData = new LogData();
                        logData.setType(UserLogTypeEnum.BETRATE.getValue());
                        logData.setName("用户各赛种货量百分比");
                        logData.setOldData( oldUserMap.get(entry.getKey())==null ? "" : JSONObject.toJSONString(oldUserMap.get(entry.getKey())));
                        logData.setData(JSONObject.toJSONString(entry.getValue()));
                        logDataList.add(logData);
                        //操作人
                        LogData logData1=new LogData();
                        logData1.setType(UserLogTypeEnum.TRADER.getValue());
                        logData1.setName("操作人");
                        logData1.setData(modifyUser);
                        logDataList.add(logData1);
                        RcsOperationLog rcsOperationLog = new RcsOperationLog();
                        rcsOperationLog.setHandleCode("user_config_history");
                        rcsOperationLog.setHanlerId(String.valueOf(entry.getKey()));
                        rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));
                        rcsOperationLog.setShowContent("16");
                        rcsOperationLogList.add(rcsOperationLog);
                    }

                    log.info("USER_GROUP_BET_RATE_TOPIC用户共{}个,需要保存{}条日志", userList.size(), rcsOperationLogList.size());
                    if (!CollectionUtils.isEmpty(rcsOperationLogList)){
                        rcsOperationLogMapper.saveBatchRcsOperationLog(rcsOperationLogList);
                        log.info("USER_GROUP_BET_RATE_TOPIC日志处理完成");
                    }
                }else{
                    log.info("USER_GROUP_BET_RATE_TOPIC无用户需要处理日志");
                }
            }
        } catch (Exception e) {
            log.error("::{}::根据玩家组id修改所有所属玩家的货量百分比配置异常：{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
