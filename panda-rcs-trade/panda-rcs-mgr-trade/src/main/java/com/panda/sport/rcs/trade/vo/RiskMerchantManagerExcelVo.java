package com.panda.sport.rcs.trade.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 导出excel对象
 *
 * @author derre
 * @date 2022-03-28
 */
@Data
public class RiskMerchantManagerExcelVo {

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID", index = 0)
    @ColumnWidth(value = 18)
    private String userId;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名", index = 1)
    @ColumnWidth(value = 18)
    private String userName;

    /**
     * 商户编号
     */
    @ExcelProperty(value = "商户", index = 2)
    @ColumnWidth(value = 18)
    private String merchantCode;

    /**
     * 风控类型,1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
     */
    @ExcelProperty(value = "风控类型", index = 3)
    @ColumnWidth(value = 18)
    private String type;

    /**
     * 风控建议设置值
     */
    @ExcelProperty(value = "平台建议设置值", index = 4)
    @ColumnWidth(value = 40)
    private String recommendValue;

    /**
     * 风控补充说明
     */
    @ExcelProperty(value = "平台风控补充说明", index = 5)
    @ColumnWidth(value = 18)
    private String supplementExplain;

    /**
     * 风控建议时间
     */
    @ExcelProperty(value = "风控建议时间", index = 6)
    @ColumnWidth(value = 18)
    private String recommendTime;

    /**
     * 风控建议人
     */
    @ExcelProperty(value = "建议人", index = 7)
    @ColumnWidth(value = 18)
    private String riskOperator;

    /**
     * 状态:0待处理,1同意,2拒绝,3强制执行
     */
    @ExcelProperty(value = "商户处理状态", index = 8)
    @ColumnWidth(value = 18)
    private String status;

    /**
     * 商户处理人
     */
    @ExcelProperty(value = "商户处理人", index = 9)
    @ColumnWidth(value = 18)
    private String merchantOperator;

    /**
     * 商户处理时间
     */
    @ExcelProperty(value = "商户处理时间", index = 10)
    @ColumnWidth(value = 18)
    private String processTime;
    /**
     * 商户处理说明
     */
    @ExcelProperty(value = "商户处理说明", index = 11)
    @ColumnWidth(value = 18)
    private String merchantRemark;


}
