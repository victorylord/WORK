package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import lombok.Data;

import java.util.List;

/**
 * 赛事比分详情
 */
@Data
public class MatchStatisticsInfoDetailVO {

    private Long time;

    private Long standardMatchId;

    List<MatchStatisticsInfoDetail> statisticsList;
}
