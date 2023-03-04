package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.merge.dto.Request;
import com.panda.sport.rcs.mapper.RcsBroadCastMapper;
import com.panda.sport.rcs.mapper.RcsTraderMessageMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsTraderMessage;
import com.panda.sport.rcs.pojo.vo.OperateMessageVo;
import com.panda.sport.rcs.pojo.vo.RcsBroadCastVo;
import com.panda.sport.rcs.trade.enums.MsgTypeEnum;
import com.panda.sport.rcs.trade.mq.impl.MessageConsumerUtil;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.ClickSecondVo;
import com.panda.sport.rcs.trade.wrapper.RcsBroadCastService;
import com.panda.sport.rcs.trade.wrapper.RcsTraderMessageService;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsBroadCastONEVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  消息
 * @Date: 2020-09-16 18:28
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@RequestMapping(value = "/matchTrade")
@Slf4j
@Component
public class MessageController {

    @Autowired
    private RcsTraderMessageService rcsTraderMessageService;
    @Autowired
    private RcsTraderMessageMapper traderMessageMapper;
    @Autowired
    private RcsBroadCastService rcsBroadCastService;
    @Autowired
    private RcsBroadCastMapper rcsBroadCastMapper;

    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private MessageConsumerUtil messageConsumerUtil;
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    @Autowired
    private TradeVerificationService tradeVerificationService;

    @Autowired
    private RedisUtils redisUtils;


    private final  static  long  MIN_TIME=24 * 60 * 60 * 1000;
    private final  static  long  NOTICE_MIN_TIME=30 * 60 * 1000;

    @PostMapping("/readMessage")
    public HttpResponse readMessage(@RequestBody RcsTraderMessage message) throws Exception {
        if(null==message.getMessageId()){
            return HttpResponse.fail("messageId不能为空");
        }
        String s = String.valueOf(TradeUserUtils.getUserId());
        try {
            traderMessageMapper.insertIgnoreInto(message.getMessageId(),s);
            sendNoReaNum(s);
            return HttpResponse.success(standardMatchInfoMapper.getSportId(message.getMessageId().intValue()));
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"插入数据失败");
        }
    }


    @PostMapping("/messageList")
    public HttpResponse messageList(@RequestBody RcsTraderMessage message) {
        if(null==message.getSportId()){
            return HttpResponse.fail("sportId不能为空");
        }
        if(message.getSportId().equals(2)){
            return HttpResponse.fail("篮球界面无需提醒");
        }
        try {
            String lang = TradeUserUtils.getLang();
            Integer userId = TradeUserUtils.getUserId();
            long createTime = System.currentTimeMillis() - NOTICE_MIN_TIME;
            List<RcsBroadCastVo> rcsBroadCastVoList = rcsBroadCastMapper.selectNoticeByWarningAndSealing(message.getMatchStatus(), userId, createTime);
            if (!CollectionUtils.isEmpty(rcsBroadCastVoList)){
                for (RcsBroadCastVo rcsBroadCastVo:rcsBroadCastVoList){
                    String content = rcsBroadCastVo.getContent();
                    boolean valid = jsonValid(content);
                    if (valid){
                        try {
                            Map<String, Object> stringObjectMap = JSONObject.parseObject(content);
                            rcsBroadCastVo.setContent(stringObjectMap.get(lang).toString());
                        }catch (Exception e){
                            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                        }
                    }
                    String extendsField1 = rcsBroadCastVo.getExtendsField1();
                    boolean valid1 = jsonValid(extendsField1);
                    if (valid1){
                        try {
                            Map<String, Object> stringObjectMap = JSONObject.parseObject(extendsField1);
                            rcsBroadCastVo.setExtendsField1(stringObjectMap.get(lang).toString());
                        }catch (Exception e){
                            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                        }

                    }
                }
            }
            return HttpResponse.success(rcsBroadCastVoList);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("系统异常");
        }
    }



    @RequestMapping(value = "/clickSecond",method = RequestMethod.POST)
    public HttpResponse clickSecond(@RequestBody ClickSecondVo clickSecondVo){
        try {
            Integer msgType = clickSecondVo.getMsgType();
            Integer tradeId = TradeUserUtils.getUserId();
            long createTime = System.currentTimeMillis() - MIN_TIME;
            List<Integer> msgIdList = new ArrayList<>();
            if (msgType.equals(MsgTypeEnum.WARNING.getMsgType())) {
                msgIdList = rcsBroadCastMapper.selectNoReadRcsBroadCastIdByWarningAndSealing(MsgTypeEnum.WARNING.getMsgType(),  tradeId,  createTime);
            }else if (msgType.equals(MsgTypeEnum.SEALING.getMsgType())){
                msgIdList = rcsBroadCastMapper.selectNoReadRcsBroadCastIdByWarningAndSealing(MsgTypeEnum.SEALING.getMsgType(),  tradeId,  createTime);
            }else if (msgType.equals(MsgTypeEnum.SETTLEMENT.getMsgType())){
                msgIdList=getNoReadRcsBroadCastIdBySettlement(MsgTypeEnum.SETTLEMENT.getMsgType(),  tradeId,  createTime);
            }else if (msgType.equals(MsgTypeEnum.ERROR_END.getMsgType())){
                msgIdList=rcsBroadCastMapper.selectNoReadRcsBroadCastIdByWarningAndSealing(MsgTypeEnum.ERROR_END.getMsgType(),  tradeId,  createTime);
            }else if (msgType.equals(MsgTypeEnum.ORDER_UNSETTLE.getMsgType())){
                msgIdList=rcsBroadCastMapper.selectNoReadRcsBroadCastIdByWarningAndSealing(MsgTypeEnum.ORDER_UNSETTLE.getMsgType(),  tradeId,  createTime);
            }
            if (!CollectionUtils.isEmpty(msgIdList)){
                List<RcsTraderMessage> rcsTraderMessageList=new ArrayList<>();
                for (Integer msgId:msgIdList){
                    RcsTraderMessage rcsTraderMessage=new RcsTraderMessage();
                    rcsTraderMessage.setIsRead(1);
                    rcsTraderMessage.setMessageId(msgId.longValue());
                    rcsTraderMessage.setTraderId(String.valueOf(tradeId.longValue()));
                    rcsTraderMessageList.add(rcsTraderMessage);
                }
                rcsTraderMessageService.saveBatch(rcsTraderMessageList);
            }
            sendNoReaNum(String.valueOf(tradeId));
            return HttpResponse.success();
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("系统异常");        }
    }


    private List<Integer> getNoReadRcsBroadCastIdBySettlement( Integer msgType, Integer traderId, Long createTime){
        List<Integer> msgIdList=new ArrayList<>();
        messageConsumerUtil.initData();
        List<Integer> allTradeUserIdList = messageConsumerUtil.getAllTradeUserIdList();
        List<Integer> riskUserIdList = messageConsumerUtil.getRiskUserIdList();
        List<Integer> settlementUsers = messageConsumerUtil.getSettlementUsers();
        int isTrade = 0;
        if (!CollectionUtils.isEmpty(allTradeUserIdList) && allTradeUserIdList.contains(traderId)) {
            //是操盘部的人
            isTrade=1;
        }
        if ((!CollectionUtils.isEmpty(riskUserIdList) && riskUserIdList.contains(traderId)) || (!CollectionUtils.isEmpty(settlementUsers) && settlementUsers.contains(traderId)) ){
            //是风控权限部的
            isTrade=2;
        }
        if (isTrade != 0) {
            List<Map<String, Object>> authUserByOperation = messageConsumerUtil.getAuthUserByOperation();
            List<Integer> sportIds = getSportIds(authUserByOperation, traderId);
            if (msgType.equals(MsgTypeEnum.SETTLEMENT.getMsgType())) {
                if (CollectionUtils.isEmpty(sportIds)) {
                    msgIdList = rcsBroadCastMapper.selectNoReadRcsBroadCastIdBySettlement( traderId, null, createTime, isTrade);
                } else {
                    msgIdList = rcsBroadCastMapper.selectNoReadRcsBroadCastIdBySettlement(traderId, sportIds, createTime, isTrade);
                }
            }
        }
        return msgIdList;
    }
    /**
     * 获取消息数据
     * @param msgType
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/tradingGetList",method = RequestMethod.GET)
    public HttpResponse tradingGetList(Integer msgType,Integer pageNum,Integer pageSize) {
        try {
            log.info("::{}::执行获取消息数据msgType:==>{}", msgType,CommonUtil.getRequestId());

            Long timeOut = 30L;
            String timeOutKey = "rcs:trade:tradingGetList:timeOut";
            timeOutKey = redisUtils.get(timeOutKey);
            if(org.apache.commons.lang3.StringUtils.isNotBlank(timeOutKey)){
                timeOut = Long.parseLong(timeOutKey);
            }

            Integer tradeId = TradeUserUtils.getUserId();
            String key = "rcs:trade:tradingGetList:msgType.%s:tradeId:%s:pageNum:%s:pageSize:%s";
            key = String.format(key, msgType, tradeId, pageNum, pageSize);
            String cache = redisUtils.get(key);
            if(org.apache.commons.lang3.StringUtils.isNotBlank(cache)){
                List<OperateMessageVo> operateMessageVoList = JSONObject.parseArray(cache, OperateMessageVo.class);
                log.info("::{}::消息读取缓存:" + key,CommonUtil.getRequestId());
                return HttpResponse.success(operateMessageVoList);
            }

            //1：参数验证
            List<OperateMessageVo> operateMessageVoList=new ArrayList<>();
            if (pageNum == null) {
                pageNum = 1;
            }
            if (pageSize == null) {
                pageSize = 20;
            }
            if (msgType==null){
				log.error("::{}::msgType不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("msgType不能为空");
            }

            long createTime = System.currentTimeMillis() - 3 * 60 * 60 * 1000;
            String cacheTime = redisUtils.get("rcs:tradingGetList:time");
            if(!StringUtils.isBlank(cacheTime)) {
            	try {
            		createTime = System.currentTimeMillis() - Integer.parseInt(cacheTime) * 60 * 60 * 1000;
            	}catch (Exception e) {
                    log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            	}
            }

            //预警消息标签
            operateMessageVoList.add(getOperateMessageVoByWarning(msgType, pageNum, pageSize, tradeId, createTime));
            //结算消息
            operateMessageVoList.add(getOperateMessageVoBySettlement(msgType, pageNum, pageSize, tradeId, createTime));
            //封盘消息标签
            operateMessageVoList.add(getOperateMessageVoBySealing(msgType, pageNum, pageSize, tradeId, createTime));
            //异常闭赛
            operateMessageVoList.add(getMatchErrorOver(msgType, pageNum, pageSize, tradeId, createTime));
            //未结算闭赛
            operateMessageVoList.add(getOrderUnsettle(msgType, pageNum, pageSize, tradeId, createTime));
            String lang = TradeUserUtils.getLang();
            if (!CollectionUtils.isEmpty(operateMessageVoList)){
                for (OperateMessageVo operateMessageVo:operateMessageVoList){
                    List<RcsBroadCastVo> rcsBroadCastVoList = operateMessageVo.getRcsBroadCastVoList();
                    if (!CollectionUtils.isEmpty(rcsBroadCastVoList)){
                        for (RcsBroadCastVo rcsBroadCastVo : rcsBroadCastVoList) {
                            String content = rcsBroadCastVo.getContent();
                            boolean valid = jsonValid(content);
                            if (valid) {
                                try {
                                    Map<String, Object> stringObjectMap = JSONObject.parseObject(content);
                                    rcsBroadCastVo.setContent(stringObjectMap.get(lang).toString());
                                } catch (Exception e) {
                                    log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                                }
                            }
                            String extendsField1 = rcsBroadCastVo.getExtendsField1();
                            boolean valid1 = jsonValid(extendsField1);
                            if (valid1) {
                                try {
                                    Map<String, Object> stringObjectMap = JSONObject.parseObject(extendsField1);
                                    rcsBroadCastVo.setExtendsField1(stringObjectMap.get(lang).toString());
                                } catch (Exception e) {
                                    log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
            redisUtils.setex(key, JSONObject.toJSONString(operateMessageVoList), timeOut, TimeUnit.SECONDS);
            return HttpResponse.success(operateMessageVoList);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("系统异常");
        }
    }

    private OperateMessageVo getOrderUnsettle(Integer msgType, Integer pageNum, Integer pageSize, Integer tradeId, long createTime) {
        OperateMessageVo operateMessageVo=new OperateMessageVo();
        operateMessageVo.setMsgType(MsgTypeEnum.ORDER_UNSETTLE.getMsgType());
        //1:先计算数量
        List<RcsBroadCastONEVO> rcsBroadCastONEVOList = rcsBroadCastMapper.selectRcsBroadCastCountByMatchErrorEventEnd(MsgTypeEnum.ORDER_UNSETTLE.getMsgType(), tradeId, createTime);
        int noRead=0;
        int total=0;
        if (!CollectionUtils.isEmpty(rcsBroadCastONEVOList)){
            for (RcsBroadCastONEVO rcsBroadCastONEVO:rcsBroadCastONEVOList){
                if (rcsBroadCastONEVO.getIsRead()==null){
                    noRead+=rcsBroadCastONEVO.getCount();
                }
                total+=rcsBroadCastONEVO.getCount();
            }
        }
        operateMessageVo.setTotal(total);
        operateMessageVo.setNoReadTotal(noRead);
        //2:查询具体的数据
        if (msgType.equals(MsgTypeEnum.ORDER_UNSETTLE.getMsgType())) {
            List<RcsBroadCastVo> rcsBroadCastVoList = rcsBroadCastMapper.selectRcsBroadCastByMatchErrorEventEnd(msgType, tradeId, createTime, (pageNum-1) * pageSize, pageSize);
            operateMessageVo.setRcsBroadCastVoList(rcsBroadCastVoList);
        }
        return operateMessageVo;
    }

    private OperateMessageVo getMatchErrorOver(Integer msgType, Integer pageNum, Integer pageSize, Integer tradeId, long createTime) {
        OperateMessageVo operateMessageVo=new OperateMessageVo();
        operateMessageVo.setMsgType(MsgTypeEnum.ERROR_END.getMsgType());
        //1:先计算数量
        List<RcsBroadCastONEVO> rcsBroadCastONEVOList = rcsBroadCastMapper.selectRcsBroadCastCountByMatchErrorEventEnd(MsgTypeEnum.ERROR_END.getMsgType(), tradeId, createTime);
        int noRead=0;
        int total=0;
        if (!CollectionUtils.isEmpty(rcsBroadCastONEVOList)){
            for (RcsBroadCastONEVO rcsBroadCastONEVO:rcsBroadCastONEVOList){
                if (rcsBroadCastONEVO.getIsRead()==null){
                    noRead+=rcsBroadCastONEVO.getCount();
                }
                total+=rcsBroadCastONEVO.getCount();
            }
        }
        operateMessageVo.setTotal(total);
        operateMessageVo.setNoReadTotal(noRead);
        //2:查询具体的数据
        if (msgType.equals(MsgTypeEnum.ERROR_END.getMsgType())) {
            List<RcsBroadCastVo> rcsBroadCastVoList = rcsBroadCastMapper.selectRcsBroadCastByMatchErrorEventEnd(msgType, tradeId, createTime, (pageNum-1) * pageSize, pageSize);
            operateMessageVo.setRcsBroadCastVoList(rcsBroadCastVoList);
        }
        return operateMessageVo;
    }

    /**
     * 查询结算消息数据
     * @param msgType
     * @param pageNum
     * @param pageSize
     * @param tradeId
     * @param createTime
     * @return
     */
    private OperateMessageVo getOperateMessageVoBySettlement(Integer msgType,Integer pageNum,Integer pageSize,Integer tradeId,Long createTime){
        OperateMessageVo operateMessageVo=new OperateMessageVo();
        messageConsumerUtil.initData();
        List<Integer> allTradeUserIdList = messageConsumerUtil.getAllTradeUserIdList();
        List<Integer> riskUserIdList = messageConsumerUtil.getRiskUserIdList();
        List<Integer> settlementUsers = messageConsumerUtil.getSettlementUsers();
        List<RcsBroadCastVo> records=new ArrayList<>();
        int isTrade = 0;
        if (!CollectionUtils.isEmpty(allTradeUserIdList) && allTradeUserIdList.contains(tradeId)) {
            //是操盘部的人
            isTrade=1;
        }
        if ( (!CollectionUtils.isEmpty(riskUserIdList) && riskUserIdList.contains(tradeId)) || (!CollectionUtils.isEmpty(settlementUsers) && settlementUsers.contains(tradeId))){
            //是风控权限部的  或结算组人员
            isTrade=2;
        }
        if (isTrade != 0) {
            List<Map<String, Object>> authUserByOperation = messageConsumerUtil.getAuthUserByOperation();
            List<Integer> sportIds = getSportIds(authUserByOperation, tradeId);
            if (msgType.equals(MsgTypeEnum.SETTLEMENT.getMsgType())) {
                if (CollectionUtils.isEmpty(sportIds)) {
                    records = rcsBroadCastService.queryRcsBroadCastVo(pageNum, pageSize, tradeId, null, createTime, isTrade);
                } else {
                    records = rcsBroadCastService.queryRcsBroadCastVo(pageNum, pageSize, tradeId, sportIds, createTime, isTrade);
                }
            }
            if (CollectionUtils.isEmpty(sportIds)) {
                operateMessageVo = rcsBroadCastService.queryRcsBroadCastVoIsNoRead(tradeId, null, createTime, isTrade);
            } else {
                operateMessageVo = rcsBroadCastService.queryRcsBroadCastVoIsNoRead(tradeId, sportIds, createTime, isTrade);
            }
        }
        operateMessageVo.setRcsBroadCastVoList(records);
        operateMessageVo.setPageNum(pageNum);
        operateMessageVo.setPageSize(pageSize);
        if ( operateMessageVo.getTotal()!=null) {
            operateMessageVo.setNoReadTotal(operateMessageVo.getTotal() - operateMessageVo.getReadTotal());
        }else {
            operateMessageVo.setNoReadTotal(0);
            operateMessageVo.setTotal(0);
            operateMessageVo.setReadTotal(0);
        }
        operateMessageVo.setMsgType(MsgTypeEnum.SETTLEMENT.getMsgType());
        return operateMessageVo;
    }

    /**
     * 查询预警消息数据
     * @param msgType
     * @param pageNum
     * @param pageSize
     * @param tradeId
     * @param createTime
     * @return
     */
    private OperateMessageVo getOperateMessageVoByWarning(Integer msgType,Integer pageNum,Integer pageSize,Integer tradeId,Long createTime){
        OperateMessageVo operateMessageVo=new OperateMessageVo();
        operateMessageVo.setMsgType(MsgTypeEnum.WARNING.getMsgType());
        //1:先计算数量
        List<RcsBroadCastONEVO> rcsBroadCastONEVOList = rcsBroadCastMapper.selectRcsBroadCastCountByWarningAndSealing(MsgTypeEnum.WARNING.getMsgType(), tradeId, createTime);
        int noRead=0;
        int total=0;
        if (!CollectionUtils.isEmpty(rcsBroadCastONEVOList)){
            for (RcsBroadCastONEVO rcsBroadCastONEVO:rcsBroadCastONEVOList){
                if (rcsBroadCastONEVO.getIsRead()==null){
                    noRead+=rcsBroadCastONEVO.getCount();
                }
                total+=rcsBroadCastONEVO.getCount();
            }
        }
        operateMessageVo.setTotal(total);
        operateMessageVo.setNoReadTotal(noRead);
        //2:查询具体的数据
        if (msgType.equals(MsgTypeEnum.WARNING.getMsgType())) {
            List<RcsBroadCastVo> rcsBroadCastVoList = rcsBroadCastMapper.selectRcsBroadCastByWarningAndSealing(msgType, tradeId, createTime, (pageNum-1) * pageSize, pageSize);
            operateMessageVo.setRcsBroadCastVoList(rcsBroadCastVoList);
        }
        return operateMessageVo;
    }

    /**
     * 查询封盘消息数据
     * @param msgType
     * @param pageNum
     * @param pageSize
     * @param tradeId
     * @param createTime
     * @return
     */
    private OperateMessageVo getOperateMessageVoBySealing(Integer msgType,Integer pageNum,Integer pageSize,Integer tradeId,Long createTime){
        OperateMessageVo operateMessageVo=new OperateMessageVo();
        operateMessageVo.setMsgType(MsgTypeEnum.SEALING.getMsgType());
        //1:先计算数量
        List<RcsBroadCastONEVO> rcsBroadCastONEVOList = rcsBroadCastMapper.selectRcsBroadCastCountByWarningAndSealing(MsgTypeEnum.SEALING.getMsgType(), tradeId, createTime);
        int noRead=0;
        int total=0;
        if (!CollectionUtils.isEmpty(rcsBroadCastONEVOList)){
            for (RcsBroadCastONEVO rcsBroadCastONEVO:rcsBroadCastONEVOList){
                if (rcsBroadCastONEVO.getIsRead()==null){
                    noRead+=rcsBroadCastONEVO.getCount();
                }
                total+=rcsBroadCastONEVO.getCount();
            }
        }
        operateMessageVo.setTotal(total);
        operateMessageVo.setNoReadTotal(noRead);
        //2:查询具体的数据
        if (msgType.equals(MsgTypeEnum.SEALING.getMsgType())) {
            List<RcsBroadCastVo> rcsBroadCastVoList = rcsBroadCastMapper.selectRcsBroadCastByWarningAndSealing(msgType, tradeId, createTime, (pageNum-1) * pageSize, pageSize);
            operateMessageVo.setRcsBroadCastVoList(rcsBroadCastVoList);
        }
        return operateMessageVo;
    }

    private List<Integer> getSportIds( List<Map<String, Object>> authUserByOperation,int traderId){
        List<Integer> list=new ArrayList<>();
        if (!CollectionUtils.isEmpty(authUserByOperation)){
            for (Map<String, Object> map:authUserByOperation){
                List<Integer> ids = (List<Integer>) map.get("ids");
                for (int s:ids){
                    if (s==traderId){
                        String orgName = (String)map.get("orgName");
                        list.add(getSportId(orgName));
                    }
                }
            }
        }
        return list;
    }

    private Integer getSportId(String s){
        if (s.contains("美式足球")){
            return 6;
        }else if (s.contains("篮球")){
            return 2;
        } else if (s.contains("足球")){
            return 1;
        }else if (s.contains("棒球")){
            return 3;
        }else if (s.contains("冰球")){
            return 4;
        }else if (s.contains("网球")){
            return 5;
        }else if (s.contains("斯诺克")){
            return 7;
        }else if (s.contains("乒乓球")){
            return 8;
        }else if (s.contains("排球")){
            return 9;
        }else if (s.contains("羽毛球")){
            return 10;
        }else if (s.contains("英雄联盟")){
            return 100;
        }else if (s.contains("dota2")){
            return 101;
        }else if (s.contains("cs")){
            return 102;
        }else if (s.contains("王者荣耀")){
            return 103;
        } else if (s.contains("绝地求生")){
            return 104;
        }else if (s.contains("政治娱乐")){
            return 105;
        }
        return 0;
    }

    /**
     * 发送未阅读消息数理
     * @param userId
     * @return
     */
    private void sendNoReaNum( String userId) {
        try {
            long createTime = System.currentTimeMillis() - MIN_TIME;
            //预警消息标签
            OperateMessageVo operateMessageVoByWarning = getOperateMessageVoByWarning(1, 1, 20, Integer.valueOf(userId), createTime);
            //结算消息
            OperateMessageVo operateMessageVoBySettlement = getOperateMessageVoBySettlement(2, 1, 20, Integer.valueOf(userId), createTime);
            //封盘消息标签
            OperateMessageVo operateMessageVoBySealing = getOperateMessageVoBySealing(3, 1, 20, Integer.valueOf(userId), createTime);
            //异常闭赛
            OperateMessageVo matchErrorOver = getMatchErrorOver(4, 1, 20, Integer.valueOf(userId), createTime);

            OperateMessageVo orderUnsettle = getOrderUnsettle(5, 1, 20, Integer.valueOf(userId), createTime);
            HashMap<String, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("traderId", userId);
            objectObjectHashMap.put("1", operateMessageVoByWarning.getNoReadTotal());
            objectObjectHashMap.put("2", operateMessageVoBySettlement.getNoReadTotal());
            objectObjectHashMap.put("3", operateMessageVoBySealing.getNoReadTotal());
            objectObjectHashMap.put("4", matchErrorOver.getNoReadTotal());
            objectObjectHashMap.put("5", orderUnsettle.getReadTotal());
            Request<HashMap> sendRequest = new Request<>();
            sendRequest.setLinkId(CommonUtil.getRequestId());
            sendRequest.setData(objectObjectHashMap);
            sendMessage.sendMessage("MESSAGE_NO_READ_NUM_TOPIC", null, CommonUtil.getRequestId(), sendRequest);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
    }

    private static boolean jsonValid(String content) {
        try {
            JSONObject.parseObject(content);
        }catch (Exception e){
            return false;
        }
        if(StringUtils.isBlank(content)||"null".equals(content)){
            return false;
        }
        return true;
        //return JSONObject.isValid(content);
    }


    public static void main(String[] args) {
        String s = String.valueOf(null);
        System.out.println(s);
    }
}
