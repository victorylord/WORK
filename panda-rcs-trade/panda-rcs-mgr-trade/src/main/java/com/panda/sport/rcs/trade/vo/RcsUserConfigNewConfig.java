package com.panda.sport.rcs.trade.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * user-config 优化配置
 * 该对象是 UserConfigNew 里面的Config字段的里面的List<对象> 做成json存储方便扩展
 *
 * @description:
 * @author: magic
 * @create: 2022-07-18 15:15
 **/
@Data
public class RcsUserConfigNewConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 体育种类Id
     */
    private Long sportId;

}