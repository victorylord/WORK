package com.panda.sport.rcs.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * @author :  charls
 * @Project Name :  panda-rcs-service
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-10-07 16:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StandardMatchStatusVo implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
     * 标准赛事ID
     */
    private Long standardMatchId;

    /**
     * 体育种类id. 运动种类id 对应sport.id
     */
    private Long sportId;

    /**
     * 比赛开始时间. 比赛开始时间 UTC时间
     */
    private Long beginTime;

    /**
     * 赛前盘下注状态. 赛前盘: 1 可下注; 0不可下注; 用于数据源控制下注状态
     */
    private Integer preMatchBetStatus;

    /**
     * 滚球下注状态. 滚球中使用: 1 可下注; 0不可下注; 用于数据源控制下注状态
     */
    private Integer liveOddsBetStatus;

    /**
     * 赛事状态.  比如:未开赛, 滚球, 取消, 延迟等.
     */
    private Integer matchStatus;

    /**
     * 是否为中立场. 取值为 0  和1  .   1:是中立场, 0:非中立场. 操盘人员可手动处理
     */
    private Integer neutralGround;

    /**
     * 数据来源编码. 取值见: data_source.code
     */
    private String dataSourceCode;

    /**
     * 第三方赛事原始id. 该厂比赛在第三方数据供应商中的id. 比如:  SportRadar 发送数据时, 这场比赛的ID.
     */
    private String thirdMatchSourceId;

    /**
     * 父赛事id
     */
    private Long parentId;

    /**
     * 赛事可下注状态. 0: betstart; 1: betstop
     */
    private Integer betStatus;

    /**
     * 比赛阶段id. 取自基础表 : match_status.id
     */
    private Long matchPeriodId;

    private MatchStatisticsInfoVo matchStatisticsInfo;
}
