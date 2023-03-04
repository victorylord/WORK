package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(updateMarketTradeType)
 * 切换操盘模式
 */
@Service
public class LogUpdateMarketTradeTypeFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        if (args == null || args.length == 0) {return  null;}
        MarketStatusUpdateVO vo = (MarketStatusUpdateVO) args[0];
        if(vo == null || vo.getOperatePageCode() == null) {return null;}

        //根據不同操作頁面組裝不同格式
        switch (vo.getOperatePageCode()) {
            case 14:
                //早盤操盤
            case 15:
                //早盤操盤 次要玩法
            case 17:
                //滾球操盤
            case 18:
                //滾球操盤 次要玩法
                return updateMarketTradeTypeFormat(rcsOperateLog, vo);
        }
        return null;
    }

    private RcsOperateLog updateMarketTradeTypeFormat(RcsOperateLog rcsOperateLog, MarketStatusUpdateVO vo) {
        MarketStatusUpdateVO oriVo = vo.getBeforeParams();
        rcsOperateLog.setOperatePageCode(vo.getOperatePageCode());
        rcsOperateLog.setMatchId(vo.getMatchId());
        rcsOperateLog.setPlayId(vo.getCategoryId());
        rcsOperateLog.setObjectIdByObj(vo.getCategoryId());
        rcsOperateLog.setObjectNameByObj(getPlayName(vo.getCategoryId(), Math.toIntExact(vo.getSportId())));
        rcsOperateLog.setExtObjectIdByObj(vo.getMatchManageId());
        rcsOperateLog.setExtObjectNameByObj(getMatchName(vo.getTeamList()));
        rcsOperateLog.setBeforeValByObj(TradeEnum.getByTradeType(oriVo.getTradeType()).getValue());
        rcsOperateLog.setAfterValByObj(TradeEnum.getByTradeType(vo.getTradeType()).getValue());
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
}
