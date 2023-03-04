package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mapper.StandardMatchTeamRelationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.param.RcsTournamentTemplatePlayOddsConfigParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 操盤日誌(modifyPlayOddsConfig)
 * 切換數據源/操盤設置-玩法賠率源設置 格式化類別
 */
@Service
public class LogDataSourceChangeFormat extends LogFormatStrategy {

    @Autowired
    private StandardMatchTeamRelationMapper matchTeamRelationMapper;
    @Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    
    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        if(args == null || args.length == 0){return null;}
        RcsTournamentTemplatePlayOddsConfigParam param = (RcsTournamentTemplatePlayOddsConfigParam) args[0];
        if(param == null || param.getOperatePageCode() == null){return null;}

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 1:
            case 14:
                //早盤操盤
            case 15:
                //早盤操盤 次要玩法
            case 17:
                //滾球操盤
            case 18:
                //滾球操盤 次要玩法
                return filterOperateType(rcsOperateLog, param);
        }
        return null;
    }

    /**
     * 判斷操作
     * @param rcsOperateLog
     * @param param
     * @return
     */
    private RcsOperateLog filterOperateType(RcsOperateLog rcsOperateLog, RcsTournamentTemplatePlayOddsConfigParam param) {

        //切換數據源
        if (Objects.nonNull(param.getBeforeParams().get("dataSource"))) {
            rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
            return operationFormat(rcsOperateLog, param);
            //玩法賠率源設置
        } else {
            switch (param.getOperatePageCode()) {
                case 14:
                    //早盘操盘-设置
                    rcsOperateLog.setOperatePageCode(110);
                    break;
                case 17:
                    //滚球操盘-设置
                    rcsOperateLog.setOperatePageCode(111);
                    break;
            }
            operateSettingFormat(rcsOperateLog, param);
            return null;
        }
    }

    /**
     * 早盤操盤 格式
     *
     * @param param
     * @return
     */
    private RcsOperateLog operationFormat(RcsOperateLog rcsOperateLog, RcsTournamentTemplatePlayOddsConfigParam param) {
        String oriDateSource = String.valueOf(Optional.ofNullable(param.getBeforeParams().get("dataSource")).orElse(""));
        String newDataSource = param.getPlayOddsConfigs().get(0).getDataSource();
        if (Objects.nonNull(newDataSource) &&
                !newDataSource.equals(oriDateSource)) {
            long playId = param.getPlayOddsConfigs().get(0).getPlayIds().get(0);

            rcsOperateLog.setMatchId(param.getMatchId());
            rcsOperateLog.setPlayId(playId);
            rcsOperateLog.setObjectIdByObj(playId);
            rcsOperateLog.setObjectNameByObj(getPlayName(playId, Math.toIntExact(param.getSportId())));
            rcsOperateLog.setExtObjectIdByObj(param.getMatchManageId());
            rcsOperateLog.setExtObjectNameByObj(getMatchName(param.getTeamList()));
            rcsOperateLog.setBeforeValByObj(oriDateSource);
            rcsOperateLog.setAfterValByObj(newDataSource);
            return rcsOperateLog;
        }
        return null;
    }

    /**
     * 玩法賠率源設置
     *
     * @param sample
     * @param param
     * @return
     */
    private void operateSettingFormat(RcsOperateLog sample, RcsTournamentTemplatePlayOddsConfigParam param) {
        sample.setMatchId(param.getMatchId());
        sample.setObjectIdByObj(param.getMatchManageId());
        sample.setObjectNameByObj(queryMatchName(Math.toIntExact(param.getMatchId())));
        sample.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        sample.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        sample.setBehavior(OperateLogEnum.OPERATE_SETTING.getName());

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
     * @param playId
     * @return
     */
    private String getPlayName(Long playId, Integer sportId) {
        LanguageInternation playName = rcsLanguageInternationMapper.getPlayNameByCategoryIdAndSportId(playId, sportId);
        return CategoryParseUtils.parseName(Objects.nonNull(playName) ? playName.getText() : "");
    }

    /**
     * 查詢玩法名稱列表
     *
     * @param playIds
     * @return
     */
    private String getPlayNameList(List<Long> playIds, Integer sportId) {
        List<LanguageInternation> playName = rcsLanguageInternationMapper.getPlayNameByPlayIds(playIds, sportId);
        List<String> playNameList = playName.stream().map(obj -> obj.getText()).collect(Collectors.toList());
        return String.join(",", playNameList);
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

    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
    }

}
