package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-26 10:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentConditionVo extends ConditionVo {
    /**
     * 所属标准区域 id.  对应  standard_sport_region.id
     */
    private Long regionId;
    /**
     * 介绍，默认为空
     */
    private String introduction;
    /**
     * 区域名称大写字母拼写
     */
    private String spell;

    /**
     * 如果当前记录对外起作用，则该visible 为 1，否则为 0。默认true
     */
    private Integer visible;

}
