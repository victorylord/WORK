package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * 导入excel批量更新
 *
 * @author derre
 * @date 2022-04-02
 */
@Data
public class RiskMerchantImportUpdateVo {

    /**
     * 用户ID
     */
    private String userId;

    /***
     * 风控类型,1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
     */
    private String type;

    /**
     * 同意 拒绝
     */
    private String status;

    /**
     * 风控补充说明
     */
    private String supplementExplain;


    /**
     * 商户处理人
     */
    private String merchantOperator;




}
