package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.param.RcsTournamentTemplatePlayOddsConfigParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操盤日誌(updatePlayOddsConfig)
 * 联赛模板日志-玩法赔率源设置
 */
@Service
public class LogUpdatePlayOddsConfigFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        RcsTournamentTemplatePlayOddsConfigParam param = (RcsTournamentTemplatePlayOddsConfigParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 21:
                //联赛参数设置
                rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
                rcsOperateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                break;
        }
        updatePlayOddsConfig(rcsOperateLog, param);

        return null;
    }

    private void updatePlayOddsConfig(RcsOperateLog sample, RcsTournamentTemplatePlayOddsConfigParam param) {
        sample.setMatchId(param.getMatchId());
        sample.setObjectIdByObj(param.getTemplateId());
        sample.setObjectNameByObj(param.getTemplateName());
        sample.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        sample.setExtObjectNameByObj(OperateLogEnum.NONE.getName());

        param.getPlayOddsConfigs().forEach(config -> {
            List<Integer> tempPlayIds = (List<Integer>) param.getBeforeParams().get(config.getDataSource());
            List<Long> oriPlayIds = tempPlayIds.stream().mapToLong(Integer::intValue).boxed().collect(Collectors.toList());

            if (compare(oriPlayIds, config.getPlayIds())) {
                RcsOperateLog rcsOperateLog = new RcsOperateLog();
                BeanUtils.copyProperties(sample, rcsOperateLog);
                rcsOperateLog.setParameterName(OperateLogEnum.PLAY_ODDS_SOURCE_CONFIG.getName() + "-" + config.getDataSource());
                String oriPlayNameStr = oriPlayIds.size() > 0 ? getPlayNameList(oriPlayIds, Math.toIntExact(param.getSportId())) : OperateLogEnum.NONE.getName();
                String newPlayNameStr = config.getPlayIds().size() > 0 ? getPlayNameList(config.getPlayIds(), Math.toIntExact(param.getSportId())) : OperateLogEnum.NONE.getName();
                rcsOperateLog.setBeforeValByObj(oriPlayNameStr);
                rcsOperateLog.setAfterValByObj(newPlayNameStr);
                pushMessage(rcsOperateLog);
            }
        });

    }


    /**
     * 比較新舊玩法是否有異動
     *
     * @param oriPlayIds
     * @param newPlayIds
     * @return
     */
    private boolean compare(List<Long> oriPlayIds, List<Long> newPlayIds) {
        Collections.sort(oriPlayIds);
        Collections.sort(newPlayIds);

        if (oriPlayIds.size() != newPlayIds.size()) {
            return true;
        } else {
            for (int i = 0; i < oriPlayIds.size(); i++) {
                if (Objects.nonNull(oriPlayIds) && Objects.nonNull(newPlayIds)) {
                    if (oriPlayIds.get(i).intValue() != newPlayIds.get(i).intValue()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 查詢玩法名稱
     *
     * @param playIds
     * @return
     */
    private String getPlayNameList(List<Long> playIds, Integer sportId) {
        List<LanguageInternation> playName = rcsLanguageInternationMapper.getPlayNameByPlayIds(playIds, sportId);
        List<String> playNameList = playName.stream().map(obj -> obj.getText()).collect(Collectors.toList());
        return String.join(",", playNameList);
    }

    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
    }
}
