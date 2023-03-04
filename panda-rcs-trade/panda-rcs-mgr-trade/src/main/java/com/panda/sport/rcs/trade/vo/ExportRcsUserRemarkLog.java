package com.panda.sport.rcs.trade.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  koala
 * @Date: 2022-02-06 11:17
 * --------  ---------  --------------------------
 */
@Data

public class ExportRcsUserRemarkLog implements Serializable {
    private static final long serialVersionUID = -5563797493611958795L;

    @ExcelProperty(value = "用户ID", index = 0)
    @ColumnWidth(value = 20)
    private String userId;
    @ExcelProperty(value = "用户名", index = 1)
    @ColumnWidth(value = 35)
    private String username;
    @ExcelProperty(value = "商户", index = 2)
    @ColumnWidth(value = 16)
    private String merchantCode;
    @ExcelProperty(value = "操作时间", index = 3)
    @ColumnWidth(value = 18)
    private String createTime;
    @ExcelProperty(value = "操作人", index = 4)
    @ColumnWidth(value = 12)
    private String createUserName;
    @ExcelProperty(value = "备注", index = 5)
    @ColumnWidth(value = 60)
    private String remark;
}
