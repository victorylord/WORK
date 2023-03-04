package com.panda.sport.rcs.pojo.dto.odds;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MatchOddsConfig {

    private String linkId;

    private Long sportId;

    private String matchId;

    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;

    /**
     * 玩法配置
     */
    private List<MatchPlayConfig> playConfigList;

    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 操作来源，1-操盘手
     */
    private Integer operateSource;

    /**
     * 消息来源，timeout-暂停，timeout_over-暂停结束，syncJob-同步任务
     */
    private String messageSource;

    /**
     * 比分，让球的时候需要刷新对应的数据
     * key 比分类型  1：是当前比分， 2：角球 3：红牌
     * value  比分值
     */
    private Map<String, String> scoreMap = new HashMap<>();

}
