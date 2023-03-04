package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  kimi
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  商户数据
 * @Date: 2019-10-05 19:09
 */
@Data
public class BussinessVo implements Serializable {
    /**
     * 商户名字
     **/

    private String childKey;
    /**
     * 商户id
     **/
    private Long value;
}
