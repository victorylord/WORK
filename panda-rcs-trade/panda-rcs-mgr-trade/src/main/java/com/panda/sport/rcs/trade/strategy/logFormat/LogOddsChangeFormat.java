package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 操盤日誌(updateMarketOddsValue)
 * 調整賠率格式化類別
 */
@Service
@Slf4j
public class LogOddsChangeFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        RcsMatchMarketConfig config = (RcsMatchMarketConfig) args[0];

        //根據不同操作頁面組裝不同格式
        switch (config.getOperatePageCode()) {
            case 14:
                //早盤操盤
            case 17:
                //滾球操盤
                return earlyMatchFormat(rcsOperateLog, config);
            case 15:
                //早盤操盤 次要玩法
            case 18:
                //滾球操盤 次要玩法
                return subPlayFormat(rcsOperateLog, config);
            case 102:
            case 103:
                //在LogUpdateMarketConfigFormat中處裡
                break;
        }

        return null;
    }

    /**
     * 早盤資料轉換
     *
     * @param rcsOperateLog
     * @param config
     * @return
     */
    private RcsOperateLog earlyMatchFormat(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config) {
        //操盤主畫面 調整賠率傳入oddsChange會除100，在此還原，調水差不會
        if (!Objects.nonNull(config.getBeforeParams().getMarketDiffValue())) {
            config.setOddsChange(config.getOddsChange().multiply(new BigDecimal("100").setScale(0, RoundingMode.DOWN)));
        }
        return filterChangeType(rcsOperateLog, config);
    }

    /**
     * 次要玩法資料轉換
     *
     * @param rcsOperateLog
     * @param config
     * @return
     */
    private RcsOperateLog subPlayFormat(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config) {
        config.setOddsChange(config.getOddsChange().multiply(new BigDecimal("100").setScale(0, RoundingMode.DOWN)));
        return filterChangeType(rcsOperateLog, config);
    }

    /**
     * 判斷調水差還調賠率
     *
     * @param rcsOperateLog
     * @param config
     * @return
     */
    private RcsOperateLog filterChangeType(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config) {
        RcsMatchMarketConfig oriConfig = config.getBeforeParams();
        rcsOperateLog.setOperatePageCode(config.getOperatePageCode());
        rcsOperateLog.setMatchId(config.getMatchId());
        rcsOperateLog.setPlayId(config.getPlayId());

        String matchName = getMatchName(config.getTeamList());

        //區別調整水差
        if (Objects.nonNull(config.getBeforeParams().getMarketDiffValue())) {
            return marketDiffChangeFormat(rcsOperateLog, config, matchName);
        } else {
            return oddsChangeFormat(rcsOperateLog, config, oriConfig, matchName);
        }
    }

    /**
     * 調整賠率格式
     *
     * @param rcsOperateLog
     * @param config
     * @param oriConfig
     * @param matchName
     * @return
     */
    private RcsOperateLog oddsChangeFormat(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config, RcsMatchMarketConfig oriConfig, String matchName) {
        //取出低賠index
        int minimumOddsIndex = getMinimumOddsIndex(oriConfig.getOddsList());

        //紀錄賠率異動
        for (int i = 0; i < oriConfig.getOddsList().size(); i++) {
            if (i == minimumOddsIndex) {
                Map<String, Object> oddsMap = oriConfig.getOddsList().get(i);
                log.info("操盤日誌-調賠率-oddsType:b:{},a:{},比對結果:{}", oriConfig.getOddsType(), config.getOddsType(), oddsMap.get("oddsType"));
                BigDecimal nameExpressionValue = new BigDecimal(Optional.ofNullable(oddsMap.get("nameExpressionValue")).orElse("0").toString());
                String id = Optional.ofNullable(oddsMap.get("id")).orElse("").toString();
                String playName = getPlayName(config.getPlayId(), config.getSportId());
                String marketValue = transMarketValue(nameExpressionValue.abs());
                StringBuilder objectName = new StringBuilder().append(oriConfig.getOddsType()).append(" (").append(marketValue).append(")");
                StringBuilder extObjectId = new StringBuilder().append(config.getMatchManageId()).append(" / ").append(config.getPlayId()).append(" / ").append(config.getMarketId());
                StringBuilder extObjectName = new StringBuilder(matchName).append(" / ").append(playName).append(" / ").append(nameExpressionValue);

                rcsOperateLog.setObjectIdByObj(id);
                rcsOperateLog.setObjectNameByObj(objectName);
                rcsOperateLog.setExtObjectIdByObj(extObjectId);
                rcsOperateLog.setExtObjectNameByObj(extObjectName);
                rcsOperateLog.setBeforeValByObj(OperateLogEnum.NONE.getName());
                rcsOperateLog.setAfterValByObj(config.getOddsChange().setScale(0, RoundingMode.DOWN).toPlainString());
                return rcsOperateLog;
            }
        }
        return null;
    }

    /**
     * 水差調整 格式
     *
     * @param rcsOperateLog
     * @param config
     * @param matchName
     * @return
     */
    private RcsOperateLog marketDiffChangeFormat(RcsOperateLog rcsOperateLog, RcsMatchMarketConfig config, String matchName) {
        //盤口值
        String marketValueView = "";
        for (Map<String, Object> oddsMap : config.getOddsList()) {
            BigDecimal nameExpressionValue = new BigDecimal(Optional.ofNullable(oddsMap.get("nameExpressionValue")).orElse("0").toString());
            marketValueView = transMarketValue(nameExpressionValue.abs());
            break;
        }

        if (Objects.nonNull(config.getOddsChange())) {
            BigDecimal marketDiffValue = config.getBeforeParams().getMarketDiffValue().setScale(0, RoundingMode.DOWN);
            BigDecimal oddsChange = config.getOddsChange().setScale(0, RoundingMode.DOWN);
            if (oddsChange.compareTo(marketDiffValue) != 0) {
                String id = getAwayBetInfo(config.getOddsList(), "id");
                String oddsType = getAwayBetInfo(config.getOddsList(), "oddsType");
                String playName = getPlayName(config.getPlayId(), config.getSportId());
                StringBuilder objectName = new StringBuilder().append(oddsType).append(" (").append(marketValueView).append(")");
                StringBuilder extObjectId = new StringBuilder().append(config.getMatchManageId()).append(" / ").append(config.getPlayId()).append(" / ").append(config.getMarketId());
                StringBuilder extObjectName = new StringBuilder(matchName).append(" / ").append(playName).append(" / ").append(marketValueView);

                rcsOperateLog.setObjectIdByObj(id);
                rcsOperateLog.setObjectNameByObj(objectName);
                rcsOperateLog.setExtObjectIdByObj(extObjectId);
                rcsOperateLog.setExtObjectNameByObj(extObjectName);
                rcsOperateLog.setBehavior(OperateLogEnum.CONFIG_UPDATE.getName());
                rcsOperateLog.setParameterName(OperateLogEnum.MARKET_DIFF.getName());

                rcsOperateLog.setBeforeValByObj(marketDiffValue.toPlainString());
                rcsOperateLog.setAfterValByObj(oddsChange.toPlainString());
                return rcsOperateLog;
            }
        }

        return null;
    }

    /**
     * 從客隊投注項取出對應參數 (目前僅調水差會使用)
     *
     * @param list
     */
    private String getAwayBetInfo(List<Map<String, Object>> list, String key) {
        Map<String, Object> tempMap = list.get(list.size() - 1);
        return String.valueOf(tempMap.getOrDefault(key, ""));
    }

    /**
     * 查表找出低賠index
     *
     * @param oddsList
     * @return
     */
    private int getMinimumOddsIndex(List<Map<String, Object>> oddsList) {
        //求出低陪index
        BigDecimal minimunPaOddsValue = BigDecimal.ZERO;
        int minimumOddsIndex = 0;

        for (int i = 0; i < oddsList.size(); i++) {
            Map<String, Object> oddsMap = oddsList.get(i);
            BigDecimal paOddsValue = new BigDecimal(Optional.ofNullable(oddsMap.get("paOddsValue")).orElse("0").toString());
            if (paOddsValue.compareTo(minimunPaOddsValue) < 1 || minimunPaOddsValue.compareTo(BigDecimal.ZERO) == 0) {
                minimumOddsIndex = i;
                minimunPaOddsValue = paOddsValue;
            }
        }
        return minimumOddsIndex;
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



