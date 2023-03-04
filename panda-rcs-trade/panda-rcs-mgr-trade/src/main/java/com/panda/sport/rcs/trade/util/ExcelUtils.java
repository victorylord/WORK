package com.panda.sport.rcs.trade.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-21 16:33
 **/
public class ExcelUtils {
    /**
     * 文件路径
     */
    public static String filePath = "/opt/logs/riskFileDown";
    /**
     * 创建表头
     * @param workbook
     * @param sheet
     */
    public static void createTitle(XSSFWorkbook workbook, XSSFSheet sheet, String [] title){
        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i < title.length; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(title[i]);
        }
    }

    /**
     * 生成excel文件
     * @param filename
     * @param workbook
     * @throws Exception
     */
    public static  String buildExcelFile(String filename,XSSFWorkbook workbook) throws Exception{


        File path=new File(filePath);
        if(!path.exists()){
            path.mkdir();
        }

        File file=new File(path.getAbsolutePath(), filename);


        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);


        fos.flush();
        fos.close();
        //filename = Base64.encodeBase64String(filename.getBytes());

        return filename;
    }

    /**
     * @Description   生成随机文件名，防止上传文件后文件名重复
     * @Param []
     * @Author  toney
     * @Date  0:08 2020/7/6
     * @return java.lang.String
     **/
    public static String generateRandomFilename(){
        String RandomFilename = "";
        Random rand = new Random();//生成随机数
        int random = rand.nextInt();

        Calendar calCurrent = Calendar.getInstance();
        int intDay = calCurrent.get(Calendar.DATE);
        int intMonth = calCurrent.get(Calendar.MONTH) + 1;
        int intYear = calCurrent.get(Calendar.YEAR);
        String now = String.valueOf(intYear) + "_" + String.valueOf(intMonth) + "_" +
            String.valueOf(intDay) + "_";

        RandomFilename = now + String.valueOf(random > 0 ? random : ( -1) * random);

        return RandomFilename;
    }
}
