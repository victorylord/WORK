package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-11-26 16:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BusinessSingleBetVo implements Serializable {
    /**
     * 联赛级别
     */
    private Integer tournamentLevel;

    private List<RcsBusinessSingleBetConfig>  singleBetConfigs;

}
