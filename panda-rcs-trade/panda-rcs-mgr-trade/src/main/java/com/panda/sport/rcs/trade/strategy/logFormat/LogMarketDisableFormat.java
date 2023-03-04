package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import com.panda.sport.rcs.vo.MarketDisableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 操盤日誌(marketDisable)
 * 設置-盘口弃用
 */
@Service
public class LogMarketDisableFormat extends LogFormatStrategy {
    
    @Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        MarketDisableVO vo = (MarketDisableVO) args[0];

        rcsOperateLog.setOperatePageCode(vo.getOperatePageCode());
        rcsOperateLog.setMatchId(vo.getMatchId());
        rcsOperateLog.setPlayId(vo.getPlayId());
        rcsOperateLog.setObjectIdByObj(vo.getPlayId());
        String marketValue = Objects.nonNull(vo.getMarketValue()) ? transMarketValue(new BigDecimal(vo.getMarketValue()).abs()) : OperateLogEnum.NONE.getName();
        StringBuilder objectName = new StringBuilder().append(getPlayName(vo.getPlayId(), Math.toIntExact(vo.getSportId()))).append("(").append(marketValue).append(")");
        rcsOperateLog.setObjectNameByObj(objectName);
        rcsOperateLog.setExtObjectIdByObj(vo.getMatchManageId());
        rcsOperateLog.setExtObjectNameByObj(getMatchName(vo.getTeamList()));
        rcsOperateLog.setParameterName(OperateLogEnum.MARKET_DISABLE.getName());
        rcsOperateLog.setBeforeValByObj(getIsSeriesName(vo.getBeforeParams().getDisableFlag()));
        rcsOperateLog.setAfterValByObj(getIsSeriesName(vo.getDisableFlag()));

        return rcsOperateLog;
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
     * 轉換狀態碼
     *
     * @param isSeries
     * @return
     */
    private String getIsSeriesName(Integer isSeries) {
        switch (isSeries) {
            case 0:
                return "启用";
            case 1:
                return "弃用";
            default:
                return "";
        }
    }
}
