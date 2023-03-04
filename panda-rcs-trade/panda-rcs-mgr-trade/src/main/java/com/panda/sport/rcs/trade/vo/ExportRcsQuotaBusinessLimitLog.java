package com.panda.sport.rcs.trade.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 商户通用设置日志
 *
 * @date 2022/07/23
 */
@Data
public class ExportRcsQuotaBusinessLimitLog implements Serializable {
    @ExcelProperty(value = "ID", index = 0)
    @ColumnWidth(value = 12)
    private Integer id;

    /**
     * 操作类别
     */
    @ExcelProperty(value = "操作类别", index = 1)
    @ColumnWidth(value = 15)
    private String operateCategory;

    /**
     * 操作对象id
     */
    @ExcelProperty(value = "操作对象ID", index = 2)
    @ColumnWidth(value = 16)
    private String objectId;

    /**
     * 操作对象名称
     */
    @ExcelProperty(value = "操作对象名称", index = 3)
    @ColumnWidth(value = 16)
    private String objectName;

    /**
     * 操作对象拓展id
     */
    @ExcelProperty(value = "操作对象拓展ID", index = 4)
    @ColumnWidth(value = 20)
    private String extObjectId;

    /**
     * 操作对象拓展名称
     */
    @ExcelProperty(value = "操作对象拓展名称", index = 5)
    @ColumnWidth(value = 22)
    private String extObjectName;

    /**
     * 操作类型
     */
    @ExcelProperty(value = "操作类型", index = 6)
    @ColumnWidth(value = 15)
    private String operateType;

    /**
     * 操作参数名称
     */
    @ExcelProperty(value = "操作参数名称", index = 7)
    @ColumnWidth(value = 17)
    private String paramName;

    /**
     * 修改前参数值
     */
    @ExcelProperty(value = "修改前参数值", index = 8)
    @ColumnWidth(value = 17)
    private String beforeVal;

    /**
     * 修改后参数值
     */
    @ExcelProperty(value = "修改后参数值", index = 9)
    @ColumnWidth(value = 17)
    private String afterVal;

    /**
     * 操作人名称
     */
    @ExcelProperty(value = "操作人名称", index = 10)
    @ColumnWidth(value = 15)
    private String userName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 11)
    @ColumnWidth(value = 17)
    private String createTime;


}