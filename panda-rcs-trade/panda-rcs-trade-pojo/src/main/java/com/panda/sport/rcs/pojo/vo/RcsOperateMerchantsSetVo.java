package com.panda.sport.rcs.pojo.vo;

import lombok.Data;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-12-02 16:29
 **/
@Data
public class RcsOperateMerchantsSetVo {
    /**
     * 主键ID
     */
    private Long  id;
    /**
     * 商户ID
     */
    private String merchantsId;
    private String name;
    private Integer status;
}
