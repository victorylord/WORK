package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class RcsMatchTradeMemo implements Serializable {

    /**
     * 备忘录记录Id
     */
    @TableId(value = "id")
    private String id;

    /**
     * 体育种类id. 运动种类id 对应sport.id
     */
    private Long sportId;

    /**
     * 标准赛事的id.对应standard_match_info.id
     */
    private Long standardMatchId;

    /**
     * 比赛开始时间.比赛开始时间UTC时间
     */
    private Long beginTime;

    /**
     * 比赛阶段id.system_item_dict.value
     */
    private Long matchPeriodId;

    /**
     * 比赛进行时间.单位:秒.例如:3分钟11秒,则该值是191
     */
    private Integer secondsMatchStart;

    /**
     * 联赛名称
     */
    private String tournamentName;

    /**
     * 主场队名称
     */
    private String home;

    /**
     * 客场队名称
     */
    private String away;

    /**
     * 主队比分
     */
    private Integer homeScore;

    /**
     * 客队比分
     */
    private Integer awayScore;

    /**
     * 操盘阶段
     */
    private Integer operateStage;

    /**
     * 操盘手id
     */
    private String traderId;

    /**
     * 操盘手名称
     */
    private String traderName;

    /**
     * 浏览次数
     */
    private Integer browseCount;

    /**
     * 历史浏览记录
     */
    private String browseHistory;

    /**
     * 记录时
     */
    private Long recordTime;

    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 备忘录内容
     */
    private String text;

    /**
     * 查询时间开始时间戳
     */
    @TableField(exist = false)
    private Long startTime;
    /**
     * 赛事状态
     */
    @TableField(exist = false)
    private Integer currentStatus;

    /**
     * 查询时间结束时间戳
     */
    @TableField(exist = false)
    private Long endTime;

    @TableField(exist = false)
    private Integer pageNo = 1;

    @TableField(exist = false)
    private Integer pageSize = 10;

    private static final long serialVersionUID = 1L;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", standardMatchId=").append(standardMatchId);
        sb.append(", beginTime=").append(beginTime);
        sb.append(", matchPeriodId=").append(matchPeriodId);
        sb.append(", secondsMatchStart=").append(secondsMatchStart);
        sb.append(", tournamentName=").append(tournamentName);
        sb.append(", home=").append(home);
        sb.append(", away=").append(away);
        sb.append(", homeScore=").append(homeScore);
        sb.append(", awayScore=").append(awayScore);
        sb.append(", operateStage=").append(operateStage);
        sb.append(", traderId=").append(traderId);
        sb.append(", traderName=").append(traderName);
        sb.append(", browseCount=").append(browseCount);
        sb.append(", browseHistory=").append(browseHistory);
        sb.append(", recordTime=").append(recordTime);
        sb.append(", modifyTime=").append(modifyTime);
        sb.append(", text=").append(text);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}