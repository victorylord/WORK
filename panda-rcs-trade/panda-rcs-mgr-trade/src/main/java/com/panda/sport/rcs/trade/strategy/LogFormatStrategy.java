package com.panda.sport.rcs.trade.strategy;

import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.pojo.MatchTeamInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 操盤日誌 OperateLog 抽象類
 *
 * @param <T>
 */
public abstract class LogFormatStrategy<T> {

    public abstract RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args);

    public void beforeProcess(Object[] args) {
    }

    /**
     * 透過隊伍列表組出 賽事名稱
     *
     * @param teamList
     * @return
     */
    protected String getMatchName(List<MatchTeamInfo> teamList) {
        //取隊伍名稱
        String home = "", away = "";
        for (MatchTeamInfo teamVo : teamList) {
            String name = Optional.ofNullable(teamVo.getText()).orElse("");
            if ("home".equals(teamVo.getMatchPosition())) {
                home = name;
            } else if ("away".equals(teamVo.getMatchPosition())) {
                away = name;
            }
        }
        return home + " VS " + away;
    }

    /**
     * 盤口值轉換
     */
    protected String transMarketValue(BigDecimal marketValue) {
        BigDecimal unit = new BigDecimal("0.25");
        if (marketValue.compareTo(BigDecimal.ZERO) == 0) {
            return String.format("%s", marketValue.stripTrailingZeros());

            // 判斷盤口值除0.25為偶數
        } else if (marketValue.divide(unit).remainder(new BigDecimal("2")).compareTo(BigDecimal.ZERO) == 0) {
            if (marketValue.compareTo(BigDecimal.ZERO) == 1) {
                return String.format("%s", marketValue.stripTrailingZeros());
            } else {
                return String.format("(-) %s", marketValue.abs().stripTrailingZeros());
            }
        } else {
            // 判斷盤口值除0.25為奇數
            if (marketValue.compareTo(BigDecimal.ZERO) == 1) {
                return String.format("%s/%s", marketValue.subtract(unit).stripTrailingZeros(), marketValue.add(unit).stripTrailingZeros());
            } else {
                return String.format("(-) %s/%s", marketValue.abs().subtract(unit).stripTrailingZeros(), marketValue.abs().add(unit).stripTrailingZeros());
            }
        }
    }

    /**
     * 秒轉換成時分秒
     *
     * @param second
     * @return
     */
    protected String secondToTime(long second) {

        long minutes = second / 60;//轉換分鐘
        second = second % 60;//剩餘秒數
        return (minutes >= 10L ? minutes : "0" + minutes) + ":" + (second >= 10L ? second : "0" + second);
    }

    /**
     * @title com.panda.sport.rcs.trade.strategy.LogFormatStrategy#oddsRuleCovert
     * @description 赔率规则处理:小数位不满补0
     * 1 大于100无需展示小数
     * 2 大于10展示1位小数
     * 3 小于10展示2位小数
     * @params [oddsValue]
     * @return java.math.BigDecimal
     * @throws
     * @date 2023/2/24 16:00
     * @author jstyChandler
     */
    protected BigDecimal oddsRuleCovert(Integer oddsValue){
        if(null == oddsValue){
            return new BigDecimal("0.00");
        }
        return new BigDecimal(oddsValue + "").divide(new BigDecimal("100000"));
    }
}
