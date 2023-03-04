package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @program: xindaima
 * @description: 操盘商户选择
 * @author: kimi
 * @create: 2020-12-02 14:56
 **/
@Data
public class RcsOperateMerchantsSet {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商户Id
     */
    private String merchantsId;
    /**\
     * 商户编码
     */
    private String merchantsCode;

    /**
     * '操盘设置的商户是否进行计算   0无效 1有效',
     */
    private Integer status;

    /**
     * '商户是否有效   0无效  1有效',
     */
    private Integer validStatus;
}
