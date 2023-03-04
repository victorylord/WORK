package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-02-19 16:23
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsMatchConfigVo implements Serializable {
    /**
     * Id
     */
    private Long id;
    /**
     * 操盘类型  0是手动  1是自动
     */
    private Integer dataSource;
}
