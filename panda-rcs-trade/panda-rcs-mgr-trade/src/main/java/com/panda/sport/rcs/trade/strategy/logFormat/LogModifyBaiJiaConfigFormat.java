package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.StandardMatchTeamRelationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.trade.param.RcsTournamentTemplateBaijiaConfigParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 操盤日誌(modifyBaiJiaConfig)
 * 联赛模板日志-更新赛事百家赔数据
 */
@Service
public class LogModifyBaiJiaConfigFormat extends LogFormatStrategy {

    @Autowired
    private StandardMatchTeamRelationMapper matchTeamRelationMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        RcsTournamentTemplateBaijiaConfigParam param = (RcsTournamentTemplateBaijiaConfigParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 14:
                //早盘操盘-设置
                rcsOperateLog.setOperatePageCode(110);
                initialOperateFormat(rcsOperateLog, param);
                break;
            case 17:
                //滚球操盘-设置
                rcsOperateLog.setOperatePageCode(111);
                initialOperateFormat(rcsOperateLog, param);
                break;
            case 21:
                rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
                initialTemplateFormat(rcsOperateLog, param);
                break;
        }
        updateConfigFormat(rcsOperateLog, param);

        return null;
    }

    private void updateConfigFormat(RcsOperateLog sample, RcsTournamentTemplateBaijiaConfigParam param) {

        //警示值
        if (Objects.nonNull(param.getCautionValue()) &&
                param.getCautionValue().compareTo(param.getBeforeParams().getCautionValue()) != 0) {
            RcsOperateLog rcsOperateLog = new RcsOperateLog();
            BeanUtils.copyProperties(sample, rcsOperateLog);
            rcsOperateLog.setParameterName(OperateLogEnum.CAUTION.getName());
            rcsOperateLog.setBeforeValByObj(param.getBeforeParams().getCautionValue());
            rcsOperateLog.setAfterValByObj(param.getCautionValue());
            pushMessage(rcsOperateLog);
        }

        //參考網 權重
        param.getBaijiaConfigs().forEach(newConfig -> {
            RcsTournamentTemplateBaijiaConfigParam.BaijiaConfig oriConfig = param.getBeforeParams().getBaijiaConfigs().stream().filter(obj -> newConfig.getName().equals(obj.getName())).findFirst().get();

            if (!newConfig.getStatus().equals(oriConfig.getStatus()) || !newConfig.getValue().equals(oriConfig.getValue())) {
                RcsOperateLog rcsOperateLog = new RcsOperateLog();
                BeanUtils.copyProperties(sample, rcsOperateLog);
                rcsOperateLog.setParameterName(OperateLogEnum.BAI_JIA_CONFIG.getName() + "-" + newConfig.getName());
                rcsOperateLog.setBeforeValByObj(transStatus(oriConfig.getStatus()) + "-" + oriConfig.getValue());
                rcsOperateLog.setAfterValByObj(transStatus(newConfig.getStatus()) + "-" + newConfig.getValue());
                pushMessage(rcsOperateLog);
            }
        });
    }

    /**
     * 初始化操盤設置 格式
     *
     * @param rcsOperateLog
     * @param param
     * @return
     */
    private RcsOperateLog initialOperateFormat(RcsOperateLog rcsOperateLog, RcsTournamentTemplateBaijiaConfigParam param) {
        rcsOperateLog.setMatchId(param.getMatchId());
        rcsOperateLog.setObjectIdByObj(param.getMatchManageId());
        rcsOperateLog.setObjectNameByObj(queryMatchName(Math.toIntExact(param.getMatchId())));
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setBehavior(OperateLogEnum.OPERATE_SETTING.getName());
        return rcsOperateLog;
    }

    /**
     * 初始化聯賽模板 格式
     *
     * @param rcsOperateLog
     * @param param
     * @return
     */
    private RcsOperateLog initialTemplateFormat(RcsOperateLog rcsOperateLog, RcsTournamentTemplateBaijiaConfigParam param) {
        rcsOperateLog.setObjectIdByObj(param.getTemplateId());
        rcsOperateLog.setObjectNameByObj(param.getTemplateName());
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());

        return rcsOperateLog;
    }


    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
    }

    /**
     * 用賽事ID查詢比賽名稱
     *
     * @param matchId
     * @return
     */
    private String queryMatchName(Integer matchId) {
        List<Map<String, Object>> teamList = matchTeamRelationMapper.selectByMatchId(matchId);
        String home = "", away = "";
        for (Map<String, Object> map : teamList) {
            String position = Optional.ofNullable(map.get("match_position")).orElse("").toString();
            String text = Optional.ofNullable(map.get("text")).orElse("").toString();
            if ("home".equals(position)) {
                home = text;
            } else if ("away".equals(position)) {
                away = text;
            }
        }
        return home + " VS " + away;
    }

    /**
     * 轉換狀態碼
     *
     * @param statusCode
     * @return
     */
    private String transStatus(Integer statusCode) {
        switch (statusCode) {
            case 0:
                return "否";
            case 1:
                return "是";
            default:
                return "";
        }
    }
}
