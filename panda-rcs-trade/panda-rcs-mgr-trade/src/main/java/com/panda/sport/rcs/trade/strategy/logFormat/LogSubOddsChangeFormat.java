package com.panda.sport.rcs.trade.strategy.logFormat;

import com.alibaba.fastjson.JSONObject;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.enums.OperateLogEnum;
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
import com.panda.sport.rcs.trade.wrapper.StandardSportTeamService;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import com.panda.sport.rcs.vo.OddsValueVo;
import com.panda.sport.rcs.vo.UpdateOddsValueVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 操盤日誌(updateMarketOddsValue)
 * 調整賠率格式化類別
 */
@Service
@Slf4j
public class LogSubOddsChangeFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    @Autowired
    private StandardSportTeamMapper standardSportTeamMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        UpdateOddsValueVo oddsValueVo = (UpdateOddsValueVo) args[0];
        Long[] subPlays = new Long[]{361L, 362L, 363L, 364L, 365L, 366L, 7L, 20L, 74L, 341L, 342L};
        if(!Arrays.asList(subPlays).contains(oddsValueVo.getPlayId())){
            return null;
        }
        List<OddsValueVo> oddsValueList = oddsValueVo.getOddsValueList();
        OddsValueVo valueVo = oddsValueList.get(0);
        StandardMarketOddsDTO orgOddsValueVo = null;
        RcsMatchMarketConfig config = new RcsMatchMarketConfig();
        config.setMatchId(oddsValueVo.getMatchId());
        config.setPlayId(oddsValueVo.getPlayId());
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        for(RcsStandardMarketDTO rcsStandardMarketDTO : playAllMarketList){
            for(StandardMarketOddsDTO oddsDTO : rcsStandardMarketDTO.getMarketOddsList()){
                if(valueVo.getOddsType().equals(oddsDTO.getOddsType())){
                    orgOddsValueVo = oddsDTO;
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
        StringBuilder extObjectId = new StringBuilder().append(config.getMatchId()).append(" / ").append(config.getPlayId()).append(" / ").append(oddsValueVo.getMarketId());
        StringBuilder extObjectName = new StringBuilder(matchName).append(" / ").append(playName).append(" / ");
        rcsOperateLog.setOperatePageCode(15);
        rcsOperateLog.setMatchId(oddsValueVo.getMatchId());
        rcsOperateLog.setPlayId(oddsValueVo.getPlayId());
        rcsOperateLog.setObjectIdByObj(orgOddsValueVo.getId());
        rcsOperateLog.setObjectNameByObj(objectName);
        rcsOperateLog.setExtObjectIdByObj(extObjectId);
        rcsOperateLog.setExtObjectNameByObj(extObjectName);
        rcsOperateLog.setBeforeValByObj(oddsRuleCovert(orgOddsValueVo.getOddsValue()));
        rcsOperateLog.setAfterValByObj(valueVo.getValue());
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



