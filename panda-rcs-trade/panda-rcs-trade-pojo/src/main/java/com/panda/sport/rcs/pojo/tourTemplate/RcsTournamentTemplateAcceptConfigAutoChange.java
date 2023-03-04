package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * rcs_tournament_template_accept_config_auto_change
 *
 */
@Data
public class RcsTournamentTemplateAcceptConfigAutoChange implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 模版id
     */
    private Long templateId;
    /**
     * 玩法集id
     */
    private Long categorySetId;

    /**
     *自动切换配置开关（0.关 1.开）
     */
    private Integer isOpen;

}
