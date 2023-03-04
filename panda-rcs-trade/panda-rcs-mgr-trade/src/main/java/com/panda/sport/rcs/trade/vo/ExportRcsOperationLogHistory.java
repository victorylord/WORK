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

public class ExportRcsOperationLogHistory implements Serializable {
    private static final long serialVersionUID = -5563797493611958795L;
    @ExcelProperty(value = "报警ID", index = 0)
    @ColumnWidth(value = 12)
    private Integer id;
    @ExcelProperty(value = "用户ID", index = 1)
    @ColumnWidth(value = 20)
    private String uid;
    @ExcelProperty(value = "用户名", index = 2)
    @ColumnWidth(value = 35)
    private String userName;
    @ExcelProperty(value = "所属商户", index = 3)
    @ColumnWidth(value = 14)
    private String name;
    @ExcelProperty(value = "类型", index = 4)
    @ColumnWidth(value = 12)
    private String type;
    @ExcelProperty(value = "操作时间", index = 5)
    @ColumnWidth(value = 18)
    private String crtTime;
    @ExcelProperty(value = "操作人", index = 6)
    @ColumnWidth(value = 12)
    private String trader;
    @ExcelProperty(value = "变更参数", index = 7)
    @ColumnWidth(value = 50)
    private String updateContent;
}
