package com.panda.sport.rcs.trade.vo.tourTemplate;

import lombok.Data;

/**
 * @Description : 联赛设置搜索条件，数据返回结构
 * @Author :  carver
 * @Date: 2020/10/21 10:00
 */
@Data
public class StandardSportTournamentVo {
    /**
     * 表ID
     */
    private Long id;
    /**
     * 联赛分级
     */
    private Integer tournamentLevel;
    /**
     * 联赛名称
     */
    private String name;
}
