package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.enums.TradeTypeEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * 操盤日誌(updateMarketWater)
 * 調水差(獨贏玩法)
 */
@Service
@Slf4j
public class LogUpdateMarketWaterFormat extends LogFormatStrategy {

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
                if (config.getTradeType() == TradeTypeEnum.AUTO.getCode()) {
                    return updateMarketWaterFormat(rcsOperateLog, config);
                } else {
                    return updateOddsFormat(rcsOperateLog, config);
                }
        }
        return null;
    }

    /**
     * 調水差格式 (會同步調整陪率)
     * @param rcsOperateLog
     * @param config
     * @return
     */
    private RcsOperateLog updateMarketWaterFormat(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config) {
        RcsMatchMarketConfig oriConfig = config.getBeforeParams();

        for (Map<String, Object> oddsMap : config.getOddsList()) {
            if (config.getOddsType().equals(oddsMap.get("oddsType"))) {
                log.info("操盤日誌-調賠率-oddsType:{},比對結果:{}", config.getOddsType(), oddsMap.get("oddsType"));
                BigDecimal newMarketDiffValue = new BigDecimal(Optional.ofNullable(oddsMap.get("marketDiffValue")).orElse("0").toString());

                //水差有異動
                if (newMarketDiffValue.compareTo(oriConfig.getMarketDiffValue()) != 0) {
                    String id = Optional.ofNullable(oddsMap.get("id")).orElse("").toString();
                    BigDecimal nameExpressionValue = new BigDecimal(Optional.ofNullable(oddsMap.get("nameExpressionValue")).orElse("0").toString());

                    String matchName = getMatchName(config.getTeamList());
                    String playName = getPlayName(config.getPlayId(), config.getSportId());
                    String marketValue = transMarketValue(nameExpressionValue.abs());
                    StringBuilder objectName = new StringBuilder().append(config.getOddsType()).append(" (").append(marketValue).append(")");
                    // extObjectId = 赛事 / 玩法 / 盘口
                    StringBuilder extObjectId = new StringBuilder().append(config.getMatchManageId()).append(" / ").append(config.getPlayId()).append(" / ").append(config.getMarketId());
                    StringBuilder extObjectName = new StringBuilder(matchName).append(" / ").append(playName).append(" / ").append(nameExpressionValue);

                    rcsOperateLog.setOperatePageCode(config.getOperatePageCode());
                    rcsOperateLog.setMatchId(config.getMatchId());
                    rcsOperateLog.setPlayId(config.getPlayId());
                    rcsOperateLog.setObjectIdByObj(id);
                    rcsOperateLog.setObjectNameByObj(objectName);
                    rcsOperateLog.setExtObjectIdByObj(extObjectId);
                    rcsOperateLog.setExtObjectNameByObj(extObjectName);
                    rcsOperateLog.setBehavior(OperateLogEnum.CONFIG_UPDATE.getName());
                    rcsOperateLog.setParameterName(OperateLogEnum.MARKET_DIFF.getName());
                    rcsOperateLog.setBeforeValByObj(oriConfig.getMarketDiffValue().setScale(0, RoundingMode.DOWN).toPlainString());
                    rcsOperateLog.setAfterValByObj(newMarketDiffValue.setScale(0, RoundingMode.DOWN).toPlainString());
                    return rcsOperateLog;
                }
            }
        }
        return null;
    }

    /**
     * 直接調整陪率格式
     *
     * @param rcsOperateLog
     * @param config
     * @return
     */
    private RcsOperateLog updateOddsFormat(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config) {

        for (Map<String, Object> oddsMap : config.getOddsList()) {
            if (config.getOddsType().equals(oddsMap.get("oddsType"))) {
                log.info("操盤日誌-調賠率-oddsType:{},比對結果:{}", config.getOddsType(), oddsMap.get("oddsType"));

                String id = Optional.ofNullable(oddsMap.get("id")).orElse("").toString();
                BigDecimal nameExpressionValue = new BigDecimal(Optional.ofNullable(oddsMap.get("nameExpressionValue")).orElse("0").toString());
                BigDecimal newOdds = new BigDecimal(Optional.ofNullable(oddsMap.get("fieldOddsValue")).orElse("0").toString());
                BigDecimal oriOdds = new BigDecimal(Optional.ofNullable(oddsMap.get("nextLevelOddsValue")).orElse("0").toString());

                if (newOdds.compareTo(oriOdds) != 0) {

                    // extObjectId = 赛事 / 玩法 / 盘口
                    String matchName = getMatchName(config.getTeamList());
                    String playName = getPlayName(config.getPlayId(), config.getSportId());
                    String marketValue = transMarketValue(nameExpressionValue.abs());
                    StringBuilder objectName = new StringBuilder().append(config.getOddsType()).append(" (").append(marketValue).append(")");
                    StringBuilder extObjectId = new StringBuilder().append(config.getMatchManageId()).append(" / ").append(config.getPlayId()).append(" / ").append(config.getMarketId());
                    StringBuilder extObjectName = new StringBuilder(matchName).append(" / ").append(playName).append(" / ").append(nameExpressionValue);

                    rcsOperateLog.setOperatePageCode(config.getOperatePageCode());
                    rcsOperateLog.setMatchId(config.getMatchId());
                    rcsOperateLog.setPlayId(config.getPlayId());
                    rcsOperateLog.setObjectIdByObj(id);
                    rcsOperateLog.setObjectNameByObj(objectName);
                    rcsOperateLog.setExtObjectIdByObj(extObjectId);
                    rcsOperateLog.setExtObjectNameByObj(extObjectName);
                    rcsOperateLog.setBehavior(OperateLogEnum.ODDS_UPDATE.getName());
                    rcsOperateLog.setParameterName(OperateLogEnum.NONE.getName());
                    rcsOperateLog.setBeforeValByObj(oriOdds.setScale(0, RoundingMode.DOWN).toPlainString());
                    rcsOperateLog.setAfterValByObj(newOdds.setScale(0, RoundingMode.DOWN).toPlainString());
                    return rcsOperateLog;
                }
            }
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
