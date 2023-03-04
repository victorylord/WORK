package com.panda.sport.rcs.trade.wrapper.impl;

import com.panda.sport.rcs.enums.ChangeLevelEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigLogsMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigLogs;
import com.panda.sport.rcs.trade.wrapper.RcsMatchMarketConfigLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.console.service.impl
 * @Description :  TODO
 * @Date: 2020-02-10 15:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMatchMarketConfigLogsServiceImpl implements RcsMatchMarketConfigLogsService {
    @Autowired
    private RcsMatchMarketConfigLogsMapper rcsMatchMarketConfigLogsMapper;


    @Override
    public void insert(RcsMatchMarketConfig rcsMatchMarketConfig) {
        RcsMatchMarketConfigLogs rcsMatchMarketConfigLogs = new RcsMatchMarketConfigLogs();
//        rcsMatchMarketConfigLogs.setNewId(rcsMatchMarketConfig.getNewId());
        rcsMatchMarketConfigLogs.setMatchId(rcsMatchMarketConfig.getMatchId());
//        rcsMatchMarketConfigLogs.setTournamentId(rcsMatchMarketConfig.getTournamentId());
        rcsMatchMarketConfigLogs.setPlayId(rcsMatchMarketConfig.getPlayId());
        rcsMatchMarketConfigLogs.setMarketId(rcsMatchMarketConfig.getMarketId());
//        rcsMatchMarketConfigLogs.setUpdateOdds(rcsMatchMarketConfig.getUpdateOdds());
        rcsMatchMarketConfigLogs.setHomeMarketValue(rcsMatchMarketConfig.getHomeMarketValue());
        rcsMatchMarketConfigLogs.setAwayMarketValue(rcsMatchMarketConfig.getAwayMarketValue());
        rcsMatchMarketConfigLogs.setMargin(rcsMatchMarketConfig.getMargin());
        rcsMatchMarketConfigLogs.setHomeLevelFirstMaxAmount(rcsMatchMarketConfig.getHomeLevelFirstMaxAmount());
        rcsMatchMarketConfigLogs.setHomeLevelFirstOddsRate(rcsMatchMarketConfig.getHomeLevelFirstOddsRate());
        rcsMatchMarketConfigLogs.setHomeLevelSecondMaxAmount(rcsMatchMarketConfig.getHomeLevelSecondMaxAmount());
        rcsMatchMarketConfigLogs.setHomeLevelSecondOddsRate(rcsMatchMarketConfig.getHomeLevelSecondOddsRate());
        rcsMatchMarketConfigLogs.setMaxSingleBetAmount(rcsMatchMarketConfig.getMaxSingleBetAmount());
        rcsMatchMarketConfigLogs.setMaxOdds(rcsMatchMarketConfig.getMaxOdds());
        rcsMatchMarketConfigLogs.setMinOdds(rcsMatchMarketConfig.getMinOdds());
        rcsMatchMarketConfigLogs.setDataSource(rcsMatchMarketConfig.getDataSource());
        rcsMatchMarketConfigLogs.setCreateUser(rcsMatchMarketConfig.getCreateUser());
        rcsMatchMarketConfigLogs.setModifyUser(rcsMatchMarketConfig.getModifyUser());
        rcsMatchMarketConfigLogs.setAwayLevelFirstOddsRate(rcsMatchMarketConfig.getAwayLevelFirstOddsRate());
        rcsMatchMarketConfigLogs.setAwayLevelSecondOddsRate(rcsMatchMarketConfig.getAwayLevelSecondOddsRate());
        rcsMatchMarketConfigLogs.setMarketStatus(rcsMatchMarketConfig.getMarketStatus());
//        rcsMatchMarketConfigLogs.setBalance(rcsMatchMarketConfig.getBalance());
        rcsMatchMarketConfigLogs.setMarketType(rcsMatchMarketConfig.getMarketType());
//        rcsMatchMarketConfigLogs.setHomeAutoChangeRate(rcsMatchMarketConfig.getHomeAutoChangeRate());
        rcsMatchMarketConfigLogs.setAwayAutoChangeRate(rcsMatchMarketConfig.getAwayAutoChangeRate());
//        rcsMatchMarketConfigLogs.setTieAutoChangeRate(rcsMatchMarketConfig.getTieAutoChangeRate());
//        rcsMatchMarketConfigLogs.setAutoBetStop(rcsMatchMarketConfig.getAutoBetStop());
        rcsMatchMarketConfigLogs.setChangeLevel(ChangeLevelEnum.MARKET.getLevel());
        //把赔率设置进去
        StringBuilder stringBuilder = new StringBuilder();
        if (rcsMatchMarketConfig.getOddsList() != null) {
            for (Map<String, Object> map : rcsMatchMarketConfig.getOddsList()) {
                if (!CollectionUtils.isEmpty(map)) {
                    stringBuilder.append(map.get("id")).append(":").append(map.get("fieldOddsValue")).append(", ");
                }
            }
        }
        rcsMatchMarketConfigLogs.setOddsValue(stringBuilder.toString());
        rcsMatchMarketConfigLogsMapper.insert(rcsMatchMarketConfigLogs);
    }

    @Override
    public List<RcsMatchMarketConfigLogs> selectRcsMatchMarketConfigLogs(Integer matchId, Long marketId) {
        Map<String, Object> columnMap = new HashMap<>();
        if (marketId != null) {
            columnMap.put("market_id", marketId);
        }
        if (matchId != null) {
            columnMap.put("match_id", matchId);
        }
        List<RcsMatchMarketConfigLogs> rcsMatchMarketConfigLogs = rcsMatchMarketConfigLogsMapper.selectByMap(columnMap);
        return rcsMatchMarketConfigLogs;
    }
}
