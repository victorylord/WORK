package com.panda.sport.rcs.vo.riskmerchantmanager;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户标签vo
 * @Date: 2022-3-27 16:32:08
 */
@Data
public class UserChangeTagVo implements Serializable {

    private Long userId;
    /**
     * 风控类型,1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
     */
    private Integer type = 1;

    /**
     * 风控补充说明
     */
    private String supplementExplain;

    /**
     * 提交类型  1 提交商户决策 2 强制执行  3非商户审核的提交
     */
    private Integer submitType;


    /**
     * 用户标签(修改后)
     */
    private Integer tagId;

    /**
     * 标签名称
     */
    private String tagName;


    /**
     * 备注
     */
    private String remark;

    /**
     * 风控操作人
     */
    private String riskOperator;

}
