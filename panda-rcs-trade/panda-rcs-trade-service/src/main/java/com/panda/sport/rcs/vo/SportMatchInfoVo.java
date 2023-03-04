package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.StandardMatchInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class SportMatchInfoVo extends StandardMatchInfo {

    private SportTournamentVo tournament;

    private List<SportTeam> teamList;

    private Map<Long, SportMarketVo> marketMap;

}
