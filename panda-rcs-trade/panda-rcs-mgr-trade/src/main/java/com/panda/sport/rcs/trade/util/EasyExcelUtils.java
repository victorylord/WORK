package com.panda.sport.rcs.trade.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

public class EasyExcelUtils {
    /**
     * 导出Excel(一个sheet)
     *
     * @param response  HttpServletResponse
     * @param list      数据list
     * @param fileName  导出的文件名
     * @param sheetName 导入文件的sheet名
     * @param clazz     实体类
     */
    public static <T> void writeExcel(HttpServletResponse response, List<T> list, String fileName, String sheetName, Class<T> clazz)throws IOException {
        OutputStream outputStream = getOutputStream(response, fileName);
        ExcelWriter excelWriter = EasyExcel.write(outputStream, clazz).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
        excelWriter.write(list, writeSheet);
        excelWriter.finish();
    }

    /**
     * 导出时生成OutputStream
     */
    private static OutputStream getOutputStream(HttpServletResponse response, String fileName) throws IOException {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(fileName+".xlsx", "UTF-8"));
            return response.getOutputStream();
    }
}