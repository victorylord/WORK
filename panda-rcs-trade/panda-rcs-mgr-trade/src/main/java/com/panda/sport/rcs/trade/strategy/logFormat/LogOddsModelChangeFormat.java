package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mapper.StandardSportTeamMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import com.panda.sport.rcs.vo.OddsValueVo;
import com.panda.sport.rcs.vo.UpdateOddsValueVo;
import com.panda.sport.rcs.vo.trade.OddsModeReqVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 操盤日誌(updateMarketOddsValue)
 * 調整賠率格式化類別
 */
@Service
@Slf4j
public class LogOddsModelChangeFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    @Autowired
    private StandardSportTeamMapper standardSportTeamMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        OddsModeReqVo oddsModeReqVo = (OddsModeReqVo) args[0];
        Long[] subPlays = new Long[]{361L, 362L, 363L, 364L, 365L, 366L, 7L, 20L, 74L, 341L, 342L};
        if(!Arrays.asList(subPlays).contains(oddsModeReqVo.getPlayId())){
            return null;
        }
        StandardMarketOddsDTO orgOddsValueVo = null;
        RcsStandardMarketDTO orgOddsMarketVo = null;
        RcsMatchMarketConfig config = new RcsMatchMarketConfig();
        config.setMatchId(oddsModeReqVo.getMatchId());
        config.setPlayId(oddsModeReqVo.getPlayId());
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        for(RcsStandardMarketDTO rcsStandardMarketDTO : playAllMarketList){
            for(StandardMarketOddsDTO oddsDTO : rcsStandardMarketDTO.getMarketOddsList()){
                if(oddsModeReqVo.getOddsType().equals(oddsDTO.getOddsType())){
                    orgOddsValueVo = oddsDTO;
                    orgOddsMarketVo = rcsStandardMarketDTO;
                    break;
                }
            }
            if(null != orgOddsValueVo){
                break;
            }
        }
        List<MatchTeamInfo> teamList = standardSportTeamMapper.queryTeamListByMatchId(config.getMatchId());
        String matchName = getMatchName(teamList);
        String playName = getPlayName(config.getPlayId(), SportIdEnum.FOOTBALL.getId().intValue());
        StringBuilder objectName = new StringBuilder(playName).append(" / ").append(orgOddsValueVo.getOddsType());
        StringBuilder extObjectId = new StringBuilder().append(config.getMatchId()).append(" / ").append(config.getPlayId()).append(" / ").append(orgOddsMarketVo.getId());
        StringBuilder extObjectName = new StringBuilder(matchName).append(" / ").append(playName).append(" / ");
        rcsOperateLog.setOperatePageCode(15);
        rcsOperateLog.setMatchId(oddsModeReqVo.getMatchId());
        rcsOperateLog.setPlayId(oddsModeReqVo.getPlayId());
        rcsOperateLog.setObjectIdByObj(orgOddsValueVo.getId());
        rcsOperateLog.setObjectNameByObj(objectName);
        rcsOperateLog.setExtObjectIdByObj(extObjectId);
        rcsOperateLog.setExtObjectNameByObj(extObjectName);
        rcsOperateLog.setBeforeValByObj(oddsRuleCovert(orgOddsValueVo.getOddsValue()));
        rcsOperateLog.setAfterValByObj(oddsRuleCovert(oddsModeReqVo.getOddsValue().intValue()));
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



