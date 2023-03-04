package com.panda.sport.rcs.matrix;

import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 矩阵请求入参
 * @Author : Kir
 * @Date : 2021-05-04 15:15
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ForecastReqVo {

    private static final long serialVersionUID = 1L;

    /**
     * 赛种
     */
    private Long sportId;
    /**
     * 联赛ID集合
     */
    private List<Long> tournamentIdList;
    /**
     * 类型，0-滚球，1-早盘，为空全部
     */
    private Integer matchType;
    /**
     * 1 所有自选 2 仅自己操盘 3 仅自己收藏 4 所有赛事
     */
    private Integer chooseType;
    /**
     * 主盘口玩法
     */
    private Long categorySetId;

    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 结束时间
     */
    private Long endTime;
    /**
     * 操盘手
     */
    private Long traderIdOrg;
    /**
     * 第几页
     */
    private Integer pageNum;
    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 当前页面需要展示的赛事
     */
    List<Long> matchIds;

    /**
     * 排序字段
     *
     * 2.time 时间正序
     * 1.tournamentLevel，nameConcat 联赛等级正序
     * 3.dimension 货量倒序
     */
    private Integer sortName;

    public Long getSportId() {
        if (sportId == null) {
            return 1L;
        }
        return sportId;
    }

    public Integer getMatchType() {
        if (matchType == null || matchType == 0 || matchType == 1) {
            return matchType;
        }
        return null;
    }

    public Integer getPageNum() {
        if (pageNum == null) {
            return 1;
        }
        return pageNum;
    }

    public Integer getPageSize() {
        if (pageSize != null) {
            return pageSize;
        }
        // 早盘只显示20场赛事，滚球只显示12场赛事，超过的分页显示
        if (getMatchType() == null || getMatchType() == 0) {
            return 12;
        }
        return 20;
    }

    private Collection<String> traderIdList;
}
