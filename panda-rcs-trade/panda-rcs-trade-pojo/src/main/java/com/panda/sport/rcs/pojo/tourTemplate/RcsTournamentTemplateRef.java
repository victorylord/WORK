package com.panda.sport.rcs.pojo.tourTemplate;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * rcs_tournament_template_ref
 * @author 
 */
@Data
public class RcsTournamentTemplateRef implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 联赛Id
     */
    private Long tournamentId;

    /**
     * 早盘模板id
     */
    private Long templateId;

    /**
     * 滚球所属模板Id
     */
    private Long liveTemplateId;

    /**
     * 是否热门，1:是 0:否
     */
    private Integer isPopular;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更改时间
     */
    private Date updateTime;
    /**
     * 综合球类接单延迟时间
     */
    private Integer orderDelayTime;
}