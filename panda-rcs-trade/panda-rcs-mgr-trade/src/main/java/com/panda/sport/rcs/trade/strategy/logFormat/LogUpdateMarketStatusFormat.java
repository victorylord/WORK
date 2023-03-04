package com.panda.sport.rcs.trade.strategy.logFormat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 操盤日誌(updateMarketStatus)
 * 開關封鎖
 */
@Service
public class LogUpdateMarketStatusFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;
    @Autowired
    private MarketCategorySetMapper marketCategorySetMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        if(args == null || args.length == 0){
            return null;
        }
        MarketStatusUpdateVO vo = (MarketStatusUpdateVO) args[0];
        if(Objects.isNull(vo) || vo.getOperatePageCode() == null){
            return null;
        }
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
                return updateMarketStatusFormat(rcsOperateLog, vo);

        }
        return null;
    }

    private RcsOperateLog updateMarketStatusFormat(RcsOperateLog rcsOperateLog, MarketStatusUpdateVO vo) {
        MarketStatusUpdateVO oriVo = vo.getBeforeParams();
        rcsOperateLog.setOperatePageCode(vo.getOperatePageCode());
        rcsOperateLog.setMatchId(vo.getMatchId());
        rcsOperateLog.setPlayId(vo.getCategoryId());
        String matchName = getMatchName(vo.getTeamList());
        switch (vo.getTradeLevel()) {
            case 1:
                //赛事级别
                if (checkStatusDiff(vo)) return null;
                rcsOperateLog.setObjectIdByObj(vo.getMatchManageId());
                rcsOperateLog.setObjectNameByObj(matchName);
                rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
                rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
                break;
            case 2:
                //玩法级别
                rcsOperateLog.setObjectIdByObj(vo.getCategoryId());
                rcsOperateLog.setObjectNameByObj(getPlayName(vo.getCategoryId(), Math.toIntExact(vo.getSportId())));
                rcsOperateLog.setExtObjectIdByObj(vo.getMatchManageId());
                rcsOperateLog.setExtObjectNameByObj(matchName);
                break;
            case 3:
                if (checkStatusDiff(vo)) return null;
                //盘口级别 (有些Level盤非盤口級別)
                if (Objects.nonNull(vo.getMarketId())) {
                    rcsOperateLog.setObjectIdByObj(vo.getMarketId());
                    rcsOperateLog.setObjectNameByObj(Objects.nonNull(vo.getMarketValue()) ? transMarketValue(new BigDecimal(vo.getMarketValue())) : OperateLogEnum.NONE.getName());
                } else {
                    rcsOperateLog.setObjectIdByObj(vo.getCategoryId());
                    rcsOperateLog.setObjectNameByObj(getPlayName(vo.getCategoryId(), Math.toIntExact(vo.getSportId())));
                }
                StringBuilder extObjectId = new StringBuilder().append(vo.getMatchManageId()).append(" / ").append(vo.getCategoryId());
                StringBuilder extObjectName = new StringBuilder(matchName).append(" / ").append(getPlayName(vo.getCategoryId(), Math.toIntExact(vo.getSportId())));
                rcsOperateLog.setExtObjectIdByObj(extObjectId);
                rcsOperateLog.setExtObjectNameByObj(extObjectName);
                break;
            case 5:
                //批量玩法级别
            case 9:
                //玩法集编码
                if (Objects.nonNull(vo.getPlaySetName())) {
                    //次要玩法
                    rcsOperateLog.setObjectIdByObj(vo.getCategorySetId());
                    rcsOperateLog.setObjectNameByObj(vo.getPlaySetName());
                } else {
                    //主玩法 主玩法開關有狀態所以可判斷
                    if (checkStatusDiff(vo)) return null;
                    rcsOperateLog.setObjectIdByObj(vo.getCategorySetId());
                    rcsOperateLog.setObjectNameByObj(getPlaySetName(vo.getPlaySetCode(), vo.getSportId()));
                }
                rcsOperateLog.setExtObjectIdByObj(vo.getMatchManageId());
                rcsOperateLog.setExtObjectNameByObj(matchName);
                break;
        }

        rcsOperateLog.setBeforeValByObj(Objects.nonNull(oriVo) ? getTradeStatusName(oriVo.getMarketStatus()) : OperateLogEnum.NONE.getName());
        rcsOperateLog.setAfterValByObj(getTradeStatusName(vo.getMarketStatus()));
        return rcsOperateLog;
    }

    /**
     * 確認開關盤狀態是否異動
     *
     * @param vo
     * @return
     */
    private boolean checkStatusDiff(MarketStatusUpdateVO vo) {
        //開關盤不一定有狀態，不一定會有before值，無before值則直接紀錄
        if (Objects.nonNull(vo.getBeforeParams())) {
            if (Objects.nonNull(vo.getMarketStatus()) &&
                    vo.getMarketStatus().compareTo(vo.getBeforeParams().getMarketStatus()) != 0) {
                return false;
            } else
                return true;
        }
        return false;
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

    private String getPlaySetName(String playSetCode, Long sportId) {
        QueryWrapper wrapper = Wrappers.query();
        wrapper.eq("play_set_code", playSetCode);
        wrapper.eq("sport_id", sportId);
        RcsMarketCategorySet marketCategorySet = marketCategorySetMapper.selectOne(wrapper);
        return Objects.nonNull(marketCategorySet) ? marketCategorySet.getName() : "";
    }


    /**
     * 根據盤口狀態碼轉換名稱
     *
     * @param stateCode
     * @return
     */
    public static String getTradeStatusName(Integer stateCode) {
        switch (stateCode) {
            case 0:
                return TradeStatusEnum.OPEN.getName();
            case 2:
                return TradeStatusEnum.CLOSE.getName();
            case 1:
                return TradeStatusEnum.SEAL.getName();
            case 11:
                return TradeStatusEnum.LOCK.getName();
            case 12:
                return TradeStatusEnum.DISABLE.getName();
            case 13:
                return TradeStatusEnum.END.getName();
            default:
                return OperateLogEnum.NONE.getName();
        }
    }

}
