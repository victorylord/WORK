package com.panda.sport.rcs.vo.mq;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 玩法开售，下发赔率源
 */
@Data
public class PlayOddsConfigVo {
    /**
     * 标准赛事id
     */
    private Long matchId;
    /**
     * 盘口类型 1：早盘；0：滚球
     */
    private Integer matchType;
    /**
     * key  数据源
     * values  玩法id
     */
    private Map<String, List<Long>> playDataSource;
}
