package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  事件/结算审核时间
 * @Date: 2020-05-12 16:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsTournamentTemplateEvent implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 事件编码
     */
    private String eventCode;

    /**
     * 事件描述
     */
    private String eventDesc;

    /**
     * 审核时间
     */
    private Integer eventHandleTime;

    /**
     *结算时间
     */
    private Integer settleHandleTime;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 创建时间
     */
    @JsonSerialize(using = DateFormatSerializer.class)
    private Date createTime;

    /**
     * 更新时间
     */
     @JsonSerialize(using = DateFormatSerializer.class)
    private Date updateTime;

    /**
     * 操作類別編碼
     */
    @TableField(exist = false)
    private Integer operatePageCode;

    /**
     * 異動前資料
     */
    @TableField(exist = false)
    private RcsTournamentTemplateEvent beforeParams;

    /**
     * 赛事id
     */
    @TableField(exist = false)
    private Long matchId;

    /**
     * 球队
     */
    @TableField(exist = false)
    private List<MatchTeamInfo> teamList;

    /**
     *  赛事管理id
     */
    @TableField(exist = false)
    private String matchManageId;
}
