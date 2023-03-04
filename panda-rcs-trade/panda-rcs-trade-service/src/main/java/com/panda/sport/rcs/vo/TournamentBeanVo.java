package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.LanguageInternation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-26 15:47
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentBeanVo extends LanguageInternation {

    /**
     * 联赛分级. 1: 一级联赛; 2:二级联赛; 3: 三级联赛; 以此类推; 0: 未分级
     */
    private Integer tournamentLevel;
}
