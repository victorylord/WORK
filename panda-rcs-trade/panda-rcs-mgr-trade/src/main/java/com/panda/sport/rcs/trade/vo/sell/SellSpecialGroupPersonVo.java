package com.panda.sport.rcs.trade.vo.sell;

import com.panda.sports.api.vo.SysOrgAuthVO;
import lombok.Data;

/**
 * 特殊关注人员表
 */
@Data
public class SellSpecialGroupPersonVo extends SysOrgAuthVO {
    /**
     * 是否已关注
     * 0：未关注
     * 1：已关注
     **/
    private Integer isSpecial = 0;
}
