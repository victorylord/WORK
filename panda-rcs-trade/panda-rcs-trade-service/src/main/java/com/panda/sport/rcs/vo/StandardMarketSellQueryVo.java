package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * 操盘手确认开售页面，前端入参接收对象
 *
 * @author carver
 */
@Data
public class StandardMarketSellQueryVo extends PageQuery {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 体育种类ID
     */
    private Long sportId;

    /**
     * 标准赛事ID
     */
    private Long matchInfoId;

    /**
     * 赛事状态  0:未开赛, 1:滚球, 2:暂停，3:结束 ，4:关闭，5:取消，6:放弃，7:延迟，8:未知，9:延期，10:中断
     */
    private Long matchStatus;

    /**
     * 开赛时间
     */
    private Long beginTime;

    /**
     * 开售时间类型  1:赛前开售时间   2：滚球开售时间
     */
    private Integer sellDateType;

    /**
     * 开售时间(格式:时间戳)
     */
    private Long sellMatchDate;

    /**
     * 排序类型  1:按时间  2:按联赛
     */
    private Integer sortType;

    /**
     * 筛选条件  0:全部  1:仅自己操作 2:已开售 3:未开售 4:无赛前操盘手 5:无滚球操盘手
     */
    private Integer filter;

    /**
     * 操作者信息
     */
    private String operatorInfo;

    /**
     * 比赛开始时间utc
     */
    private Long beginTimeMillis;

    /**
     * 比赛结束时间utc
     */
    private Long endTimeMillis;

    /**
     * 历史赛程  0:不是  1:是
     */
    private Integer historyFlag;

    /**
     * 其他早盘  0:不是  1:是
     */
    private Integer isEarlyTrading;
    /**
     * 盘口数
     */
    private Integer marketCount;

    /**
     * 角球是否展示  0:不展示 1:展示
     */
    private Integer cornerShow;

    /**
     * 罚牌是否展示  0:不展示 1:展示
     */
    private Integer cardShow;

    /**
     * 删除 0:否 1:是
     */
    private Integer isDelete;
    /**
     * 盘口类型. 属于赛前盘或者滚球盘. 1: 早盘; 0: 滚球盘.
     */
    private Integer marketType;
}
