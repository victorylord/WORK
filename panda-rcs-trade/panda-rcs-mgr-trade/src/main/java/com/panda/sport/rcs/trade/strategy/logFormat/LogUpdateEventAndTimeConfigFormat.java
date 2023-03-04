package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEvent;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 操盤日誌(updateEventAndTimeConfig)
 * 联赛模板日志-自动接拒设置-保存
 */
@Service
public class LogUpdateEventAndTimeConfigFormat extends LogFormatStrategy {

    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        RcsTournamentTemplateAcceptConfig config = (RcsTournamentTemplateAcceptConfig) args[0];

        //根據不同操作頁面組裝不同格式
        switch (config.getOperatePageCode()) {
            case 21:
                //联赛参数设置
                updateEventConfigFormat(rcsOperateLog, config);
                break;
        }
        return null;
    }

    private void updateEventConfigFormat(RcsOperateLog sample, RcsTournamentTemplateAcceptConfig config) {
        initialLogBean(sample, config);

        //T常规
        if (Objects.nonNull(config.getNormal()) &&
                !config.getNormal().equals(config.getBeforeParams().get("normal"))) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.T_NORMAL.getName());
            rcsOperateLog.setBeforeValByObj(config.getBeforeParams().get("normal"));
            rcsOperateLog.setAfterValByObj(config.getNormal());
            pushMessage(rcsOperateLog);
        }

        //T延时
        if (Objects.nonNull(config.getMinWait()) &&
                !config.getMinWait().equals(config.getBeforeParams().get("minWait"))) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.T_MIN_WAIT.getName());
            rcsOperateLog.setBeforeValByObj(config.getBeforeParams().get("minWait"));
            rcsOperateLog.setAfterValByObj(config.getMinWait());
            pushMessage(rcsOperateLog);
        }

        //Tmax
        if (Objects.nonNull(config.getMaxWait()) &&
                !config.getMaxWait().equals(config.getBeforeParams().get("maxWait"))) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.T_MAX_WAIT.getName());
            rcsOperateLog.setBeforeValByObj(config.getBeforeParams().get("maxWait"));
            rcsOperateLog.setAfterValByObj(config.getMaxWait());
            pushMessage(rcsOperateLog);
        }

        //接拒數據源
        if (Objects.nonNull(config.getDataSource()) &&
                !config.getDataSource().equals(config.getBeforeParams().get("dataSource"))) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.ACCEPT_DATA_SOURCE.getName());
            rcsOperateLog.setBeforeValByObj(config.getBeforeParams().get("dataSource"));
            rcsOperateLog.setAfterValByObj(config.getDataSource());
            pushMessage(rcsOperateLog);
        }

        //事件設定
        List<String> oriSafetyList = getOriEventNameList(config, "safety");
        List<String> oriDangerList = getOriEventNameList(config, "danger");
        List<String> oriClosingList = getOriEventNameList(config, "closing");
        List<String> oriRejectList = getOriEventNameList(config, "reject");

        Map<String, List<RcsTournamentTemplateAcceptEvent>> eventTypeMap = config.getEvents().stream().collect(groupingBy(RcsTournamentTemplateAcceptEvent::getEventType));
        List<String> newSafetyList = eventTypeMap.getOrDefault("safety", new ArrayList<>()).stream().map(RcsTournamentTemplateAcceptEvent::getEventName).collect(Collectors.toList());
        List<String> newDangerList = eventTypeMap.getOrDefault("danger", new ArrayList<>()).stream().map(RcsTournamentTemplateAcceptEvent::getEventName).collect(Collectors.toList());
        List<String> newClosingList = eventTypeMap.getOrDefault("closing", new ArrayList<>()).stream().map(RcsTournamentTemplateAcceptEvent::getEventName).collect(Collectors.toList());
        List<String> newRejectList = eventTypeMap.getOrDefault("reject", new ArrayList<>()).stream().map(RcsTournamentTemplateAcceptEvent::getEventName).collect(Collectors.toList());

        //安全事件
        if (checkEventTypeDiff(oriSafetyList, newSafetyList)) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.SAFETY_EVENT.getName());
            rcsOperateLog.setBeforeValByObj(oriSafetyList);
            rcsOperateLog.setAfterValByObj(newSafetyList);
            pushMessage(rcsOperateLog);
        }

        //危險事件
        if (checkEventTypeDiff(oriDangerList, newDangerList)) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.DANGER_EVENT.getName());
            rcsOperateLog.setBeforeValByObj(oriDangerList);
            rcsOperateLog.setAfterValByObj(newDangerList);
            pushMessage(rcsOperateLog);
        }
        //封盤事件
        if (checkEventTypeDiff(oriClosingList, newClosingList)) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.CLOSING_EVENT.getName());
            rcsOperateLog.setBeforeValByObj(oriClosingList);
            rcsOperateLog.setAfterValByObj(newClosingList);
            pushMessage(rcsOperateLog);
        }
        //拒單事件
        if (checkEventTypeDiff(oriRejectList, newRejectList)) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.REJECT_EVENT.getName());
            rcsOperateLog.setBeforeValByObj(oriRejectList);
            rcsOperateLog.setAfterValByObj(newRejectList);
            pushMessage(rcsOperateLog);
        }
    }

    private void initialLogBean(RcsOperateLog rcsOperateLog, RcsTournamentTemplateAcceptConfig config) {
        rcsOperateLog.setOperatePageCode(config.getOperatePageCode());
        rcsOperateLog.setObjectIdByObj(config.getTemplateId());
        rcsOperateLog.setObjectNameByObj(config.getTemplateName());
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setBehavior(OperateLogEnum.OPERATE_SETTING.getName());
    }

    /**
     * 檢核事件是否有異動
     *
     * @param oriList
     * @param newList
     * @return
     */
    private boolean checkEventTypeDiff(List<String> oriList, List<String> newList) {
        //安全事件
        if (newList.size() != oriList.size()) {
            return true;
        } else {
            for (String eventName : newList) {
                if (!oriList.contains(eventName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 取出修改前事件
     *
     * @param config
     * @param eventType
     * @return
     */
    private List<String> getOriEventNameList(RcsTournamentTemplateAcceptConfig config, String eventType) {
        Map<String, Object> tempMap = (Map<String, Object>) config.getBeforeParams().getOrDefault(eventType, new HashMap<>());
        List<Map<String, Object>> tempList = (List<Map<String, Object>>) tempMap.getOrDefault("data", new ArrayList<>());
        return tempList.stream().map(m -> String.valueOf(m.get("eventName"))).collect(Collectors.toList());
    }

    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
    }
}
