package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(updateMatchMarketValue)
 * 新增/調整盤口
 */
@Service
public class LogUpdateMarketValueFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;
	
    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        RcsMatchMarketConfig config = (RcsMatchMarketConfig) args[0];

        //根據不同操作頁面組裝不同格式
        switch (config.getOperatePageCode()) {
            case 14:
                //早盤操盤
            case 15:
                //早盤操盤 次要玩法
            case 17:
                //滾球操盤
            case 18:
                //滾球操盤 次要玩法
                return updateMarketValueFormat(rcsOperateLog, config);
        }
        return null;
    }

    /**
     * 早盤/滾球 調整盤口值 格式
     *
     * @param rcsOperateLog
     * @param config
     * @return
     */
    private RcsOperateLog updateMarketValueFormat(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config) {
        RcsMatchMarketConfig oriConfig = config.getBeforeParams();
        if (Objects.nonNull(config.getHomeMarketValue()) &&
                !config.getHomeMarketValue().equals(oriConfig.getHomeMarketValue())) {
            String home = "", away = "";
            for (MatchTeamInfo teamVo : config.getTeamList()) {
                if (teamVo.getMatchPosition().equals("home")) {
                    home = String.valueOf(teamVo.getNames().getOrDefault("zs", ""));
                } else if (teamVo.getMatchPosition().equals("away")) {
                    away = String.valueOf(teamVo.getNames().getOrDefault("zs", ""));
                }
            }
            // 組織拓展名稱 賽事名稱 / 玩法
            String playName = getPlayName(config.getPlayId(), config.getSportId());
            StringBuilder matchName = new StringBuilder().append(home).append(" VS ").append(away);
            rcsOperateLog.setOperatePageCode(config.getOperatePageCode());
            rcsOperateLog.setMatchId(config.getMatchId());
            rcsOperateLog.setPlayId(config.getPlayId());
            //homeMarketValue為null表示新增盤口
            if (Objects.isNull(oriConfig.getHomeMarketValueOri())) {
                rcsOperateLog.setObjectIdByObj(config.getPlayId());
                rcsOperateLog.setObjectName(playName);
                rcsOperateLog.setExtObjectIdByObj(config.getMatchManageId());
                rcsOperateLog.setExtObjectNameByObj(matchName);
                rcsOperateLog.setBehavior(OperateLogEnum.MARKET_CREATE.getName());
                rcsOperateLog.setBeforeValByObj(OperateLogEnum.NONE.getName());
            } else {
                rcsOperateLog.setObjectIdByObj(config.getOddsType());
                StringBuilder objectName = new StringBuilder().append(config.getOddsType()).append(" (").append(transMarketValue(config.getHomeMarketValue())).append(")");
                rcsOperateLog.setObjectNameByObj(objectName);
                rcsOperateLog.setExtObjectIdByObj(config.getMatchManageId() + " / " + config.getPlayId());
                rcsOperateLog.setExtObjectNameByObj(matchName.append(" / ").append(playName));
                rcsOperateLog.setBeforeValByObj(transMarketValue(oriConfig.getHomeMarketValue()));
            }
            rcsOperateLog.setAfterValByObj(transMarketValue(config.getHomeMarketValue()));
            return rcsOperateLog;
        }
        return null;
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
}
