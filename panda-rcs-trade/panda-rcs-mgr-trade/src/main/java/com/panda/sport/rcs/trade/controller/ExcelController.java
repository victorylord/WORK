package com.panda.sport.rcs.trade.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.mapper.RcsQuotaBusinessLimitLogMapper;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.vo.LogData;
import com.panda.sport.rcs.pojo.vo.RcsQuotaBusinessLimitLogReqVo;
import com.panda.sport.rcs.trade.enums.BusinessLimitLogTypeEnum;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerStatusEnum;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerTypeEnum;
import com.panda.sport.rcs.trade.service.IRiskMerchantManagerService;
import com.panda.sport.rcs.trade.service.UserRemarkRemindLogService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.EasyExcelUtils;
import com.panda.sport.rcs.trade.util.ExcelUtils;
import com.panda.sport.rcs.trade.vo.ExportRcsOperationLogHistory;
import com.panda.sport.rcs.trade.vo.ExportRcsQuotaBusinessLimitLog;
import com.panda.sport.rcs.trade.vo.ExportRcsUserRemarkLog;
import com.panda.sport.rcs.trade.vo.RiskMerchantManagerExcelVo;
import com.panda.sport.rcs.trade.wrapper.IStandardSportTypeService;
import com.panda.sport.rcs.vo.RcsOperationLogHistory;
import com.panda.sport.rcs.vo.RcsUserRemarkRemindLogQueryVo;
import com.panda.sport.rcs.vo.RiskMerchantManagerQueryVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @program: xindaima
 * @description: 导出excel
 * @author: kimi
 * @create: 2021-02-21 16:15
 **/
@RequestMapping("/excel")
@RestController
@Slf4j
public class ExcelController {
    @Autowired
    private RcsOperationLogMapper rcsOperationLogMapper;

    @Autowired
    private IStandardSportTypeService standardSportTypeService;

    @Resource
    private IRiskMerchantManagerService riskMerchantManagerService;
    @Autowired
    private IStandardSportTypeService sportTypeService;

    @Autowired
    private RcsQuotaBusinessLimitLogMapper rcsQuotaBusinessLimitLogMapper;

    @Autowired
    UserRemarkRemindLogService userRemarkRemindLogService;

    @Autowired
    private ThreadPoolTaskExecutor excelExportExecutor;

    private HashMap<Long, String> nameHashMap = new HashMap<>();

    @RequestMapping(value = "/downExcelForStatistics", method = RequestMethod.GET)
    public void excelForStatistics(@RequestParam(required = false) String user, @RequestParam(required = false) String type, @RequestParam(required = false) Long startTime, @RequestParam(required = false) Long endTime, @RequestParam(required = false) Integer total, HttpServletResponse response) throws Exception {
        try {
            log.info("用户:{},开始导出,类型:{},开始时间:{}，结束时间{}", user, type, startTime,endTime);
            List<RcsOperationLogHistory> rcsOperationLogHistoryList = getRcsOperationLogHistory(user, type, startTime,endTime, total);
            List<ExportRcsOperationLogHistory> exportRcsOperationLogHistoryLis = new ArrayList<>();
            rcsOperationLogHistoryList.forEach(rcsOperationLogHistory -> {
                ExportRcsOperationLogHistory exportRcsOperationLogHistory = new ExportRcsOperationLogHistory();
                BeanUtils.copyProperties(rcsOperationLogHistory, exportRcsOperationLogHistory);
                exportRcsOperationLogHistoryLis.add(exportRcsOperationLogHistory);
            });
            log.info("::{}::导出转换后信息:{}",CommonUtil.getRequestId(), exportRcsOperationLogHistoryLis.size());
            EasyExcelUtils.writeExcel(response, exportRcsOperationLogHistoryLis, "特殊管控提醒", "特殊管控", ExportRcsOperationLogHistory.class);
        } catch (Exception e) {
            log.error("::{}::赛事注单玩法统计导出Excel异常{}", CommonUtil.getRequestId(), e.getMessage(), e);
            e.printStackTrace();
//            return HttpResponse.failToMsg("导出失败");
        }
    }
    @RequestMapping(value = "/downExcelBusinessLimitLog", method = RequestMethod.GET)
    public void downExcelBusinessLimitLog(RcsQuotaBusinessLimitLogReqVo reqVo,HttpServletResponse response) throws Exception {
        try {
            IPage<RcsQuotaBusinessLimitLog> requestPage = new Page<>(1, 50000);
            if (StringUtils.isNotBlank(reqVo.getOperateType())){
                reqVo.setOperateType(BusinessLimitLogTypeEnum.getValue(Integer.valueOf(reqVo.getOperateType())));
            }
            long t1 = System.currentTimeMillis();
            IPage<RcsQuotaBusinessLimitLog> iPage = rcsQuotaBusinessLimitLogMapper.queryByPage(requestPage, reqVo);
            List<RcsQuotaBusinessLimitLog> list = iPage.getRecords();
            List<ExportRcsQuotaBusinessLimitLog> exportList = new ArrayList<>();
            list.forEach(limitLog -> {
                limitLog.setOperateType(setBusinessTypeName(limitLog.getOperateType()));
                ExportRcsQuotaBusinessLimitLog exportRcsQuotaBusinessLimitLog = new ExportRcsQuotaBusinessLimitLog();
                BeanUtils.copyProperties(limitLog, exportRcsQuotaBusinessLimitLog);
                exportList.add(exportRcsQuotaBusinessLimitLog);
            });
            long t2 = System.currentTimeMillis();
            log.info("::{}::导出转换后信息:{} 用时{}",CommonUtil.getRequestId(), exportList.size(),t2-t1);
            EasyExcelUtils.writeExcel(response, exportList, "风控日志", "风控日志", ExportRcsQuotaBusinessLimitLog.class);
        } catch (Exception e) {
            log.error("::{}::风控日志导出Excel异常{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
    }
    /**
     * 人工备注提醒记录表
     * */
    @ApiOperation(value = "人工备注提醒记录表导出", notes = "人工备注提醒记录表导出 ")
    @RequestMapping(value = "/downExcelRemindLog", method = RequestMethod.GET)
    public void downExcelRemindLog(RcsUserRemarkRemindLogQueryVo reqVo, HttpServletResponse response) {
        if(null == reqVo.getTotalCount()){
            reqVo.setTotalCount(1L);
        }
        if (Objects.isNull(reqVo.getPageSize()) || reqVo.getPageSize().equals(0)){
            reqVo.setPageSize(10);
        }
        int totalPages = CommonUtil.getPageCount(reqVo.getTotalCount().intValue(),reqVo.getPageSize());
        try {
            Future<List<ExportRcsUserRemarkLog>> listFuture =  excelExportExecutor.submit(() ->{
                List<ExportRcsUserRemarkLog> exportList = Collections.synchronizedList(new ArrayList<>());
                long t1 = System.currentTimeMillis();
                for (int page = 0; page < totalPages ; page++){
                    int  currentPage = page + 1;
                    if(reqVo.getPageSize() > reqVo.getTotalCount()){
                        reqVo.setPageSize(reqVo.getTotalCount().intValue());
                    }
                    reqVo.setCurrentPage(currentPage);

                    Page<RcsUserRemarkRemindLog> pageList = userRemarkRemindLogService.getUserRemarkRemindLog(reqVo);

                    pageList.getRecords().forEach(remindLog ->{
                        ExportRcsUserRemarkLog exportRcsUserRemarkLog = new ExportRcsUserRemarkLog();
                        BeanUtils.copyProperties(remindLog, exportRcsUserRemarkLog);
                        exportList.add(exportRcsUserRemarkLog);
                    });
                }
                long t2 = System.currentTimeMillis();
                log.info("::{}::查询记录:{} 用时{}",CommonUtil.getRequestId(), exportList.size(),t2-t1);
                exportList.sort(Comparator.comparing(ExportRcsUserRemarkLog::getCreateTime).reversed());
                long t3 = System.currentTimeMillis();
                log.info("::{}::导出人工备注提醒信息:{} 排序用时{}",CommonUtil.getRequestId(), exportList.size(),t3-t2);
                return exportList;
            });
            List<ExportRcsUserRemarkLog> exportList=listFuture.get();
            long t4 = System.currentTimeMillis();
            EasyExcelUtils.writeExcel(response, exportList, "人工备注提醒", "人工备注提醒", ExportRcsUserRemarkLog.class);
            long t5 = System.currentTimeMillis();
            log.info("::{}::导出人工备注提醒信息:{} 排序用时{}",CommonUtil.getRequestId(), exportList.size(),t5-t4);
        } catch (Exception e) {
            log.error("::{}::人工备注提醒导出Excel异常{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
    }
    @ApiOperation(value = "商户管控记录表导出", notes = "商户管控记录表导出 ")
    @RequestMapping(value = "/downExportMerchantManager", method = RequestMethod.GET)
    public void exportExcel(RiskMerchantManagerQueryVo param, HttpServletResponse response) {
        try {
            log.info("::{}::商户管控记录表开始导出,参数:{}",CommonUtil.getRequestId(), param);
            List<RiskMerchantManager> list = riskMerchantManagerService.list(param);
            List<RiskMerchantManagerExcelVo> resultList = new ArrayList<>();
            if (!org.apache.commons.collections.CollectionUtils.isEmpty(list)) {
                List<StandardSportType> sportTypeList = sportTypeService.list();
                list.forEach(obj -> {
                    RiskMerchantManagerExcelVo vo = new RiskMerchantManagerExcelVo();
                    BeanUtils.copyProperties(obj, vo);
                    vo.setUserId(String.valueOf(obj.getUserId()));
                    vo.setType(RiskMerchantManagerTypeEnum.getCodeDesc(obj.getType()));
                    vo.setStatus(RiskMerchantManagerStatusEnum.getCodeDesc(obj.getStatus()));
                    vo.setRecommendTime(ObjectUtils.isEmpty(obj.getRecommendTime()) ? null : DateUtils.changeDateToString(new Date(obj.getRecommendTime())));
                    vo.setProcessTime(ObjectUtils.isEmpty(obj.getProcessTime()) ? null : DateUtils.changeDateToString(new Date(obj.getProcessTime())));
                    vo.setRecommendValue(riskMerchantManagerService.getRecommendValue(obj.getType(), vo.getRecommendValue(), sportTypeList));
                    resultList.add(vo);
                });
            }
            log.info("::{}::导出转换后信息:{}",CommonUtil.getRequestId(), resultList.size());
            EasyExcelUtils.writeExcel(response, resultList, "商户管控列表", "商户管控", RiskMerchantManagerExcelVo.class);
        } catch (Exception e) {
            log.error("::{}::商户管控记录表导出Excel异常{}", CommonUtil.getRequestId(), e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String getDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @RequestMapping("/down")
    @ApiOperation(value = "Excel下载", notes = "Excel下载  name为文件名")
    public ResponseEntity<FileSystemResource> downFile(String name) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        File file = new File(ExcelUtils.filePath, name);
        //name = new String(Base64.decodeBase64(name.replace(".xls","")));
        name = name.substring(0, name.indexOf("_"));
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8") + ".xls");
        return ResponseEntity
                .ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers)
                .body(new FileSystemResource(file));
    }

    @RequestMapping("/download")
    @ApiOperation(value = "Excel下载", notes = "Excel下载  name为文件名")
    public ResponseEntity<FileSystemResource> downLoadFile(String name) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        File file = new File(ExcelUtils.filePath, name);
        name = name.substring(0, name.indexOf("."));
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(name, "utf-8") + ".xlsx");
        return ResponseEntity
                .ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers)
                .body(new FileSystemResource(file));
    }


    /**
     * 导出数据
     *
     * @param user
     * @param type
     * @param endTime
     * @param total
     * @return
     */
    @RequestMapping(value = "/getHistoryTotal", method = RequestMethod.GET)
    public void getHistoryTotal(HttpServletResponse response, @RequestParam(required = false) String user, @RequestParam(required = false) String type, @RequestParam(required = false) Long startTime,@RequestParam(required = false) Long endTime, @RequestParam(required = false) Integer total) {
        try {
            List<RcsOperationLogHistory> rcsOperationLogHistoryList = getRcsOperationLogHistory(user, type, startTime,endTime, total);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("特殊管控提醒");
            HSSFRow row1 = sheet.createRow(0);
            String[] title = new String[]{"报警ID", "用户ID", "用户名", "所属商户", "类型", "操作时间", "操作人", "变更参数"};
            for (int i = 0; i < title.length; i++) {
                HSSFCell cell = row1.createCell(i);
                HSSFRichTextString text = new HSSFRichTextString(title[i]);
                cell.setCellValue(text);
            }
            int rowNum = 1;
            if (!CollectionUtils.isEmpty(rcsOperationLogHistoryList)) {
                for (RcsOperationLogHistory rcsOperationLogHistory : rcsOperationLogHistoryList) {
                    HSSFRow row = sheet.createRow(rowNum);
                    row.createCell(0).setCellValue(rcsOperationLogHistory.getId());
                    row.createCell(1).setCellValue(rcsOperationLogHistory.getUid());
                    row.createCell(2).setCellValue(rcsOperationLogHistory.getUserName());
                    row.createCell(3).setCellValue(rcsOperationLogHistory.getName());
                    row.createCell(4).setCellValue(rcsOperationLogHistory.getType());
                    row.createCell(5).setCellValue(rcsOperationLogHistory.getCrtTime());
                    row.createCell(6).setCellValue(rcsOperationLogHistory.getTrader());
                    row.createCell(7).setCellValue(rcsOperationLogHistory.getUpdateContent());
                    rowNum++;
                }
            }
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String fileName = "users" + new Date() + ".xls";
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            log.error("::{}::导出报表失败{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
    }


    private List<RcsOperationLogHistory> getRcsOperationLogHistory(String user, String type, Long startTime,Long endTime, Integer total) {
       //产品要求 导出数量改为50000 magic by 20220806
       if (total == null) {
            total = 50000;
        } else {
            if (total > 50000) {
                total = 50000;
            }
        }
        String likeUser = null;
        if (user != null) {
            likeUser = "%" + user + "%";
        }
        List<String> types = null;
        if (type != null && !type.equals("")) {
            types = Arrays.asList(type.split("-"));
        }

        List<RcsOperationLogHistory> rcsOperationLogHistorieList = rcsOperationLogMapper.selectRcsOperationLogToatlByUser(user, types, startTime,endTime, total, likeUser);
        if (!CollectionUtils.isEmpty(rcsOperationLogHistorieList)) {
            for (RcsOperationLogHistory rcsOperationLogHistory : rcsOperationLogHistorieList) {
                String updateContent = rcsOperationLogHistory.getUpdateContent();
                List<LogData> logDataList = JSONObject.parseArray(updateContent, LogData.class);
                StringBuilder stringBuilder = new StringBuilder();
                if (!CollectionUtils.isEmpty(logDataList)) {
                    for (LogData logData : logDataList) {
                        String data = logData.getData();
                        if (logData.getName().equals("操作人")) {
                            rcsOperationLogHistory.setTrader(data);
                            continue;
                        }
                        String oldData = logData.getOldData();
                        if (oldData == null || oldData.equals("null")) {
                            oldData = "";
                        }
                        if (data == null || data.equals("null")) {
                            data = "";
                        }
                        if (!equels(oldData, data)) {
                            if (data != "") {
                                if (rcsOperationLogHistory.getType().equals("15")) {
                                    //赔率分组
                                    if (data.equals("1")) {
                                        data = "A";
                                    }
                                    if (data.equals("2")) {
                                        data = "B";
                                    }
                                    if (data.equals("3")) {
                                        data = "C";
                                    }
                                    if (data.equals("4")) {
                                        data = "D";
                                    }
                                    if (data.equals("11")) {
                                        data = "1";
                                    }
                                    if (data.equals("12")) {
                                        data = "2";
                                    }
                                    if (data.equals("13")) {
                                        data = "3";
                                    }
                                    if (data.equals("14")) {
                                        data = "4";
                                    }
                                    if (data.equals("15")) {
                                        data = "5";
                                    }
                                }

                                if (rcsOperationLogHistory.getType().equals("11")) {
                                    data = data + "s";
                                }

                                if (rcsOperationLogHistory.getType().equals("17")) {
                                    // 1无  2特殊百分比限额 3特殊单注单场限额 4特殊vip限额
                                    if (data.equals("1")) {
                                        data = "无";
                                    }
                                    if (data.equals("2")) {
                                        data = "特殊百分比限额";
                                    }
                                    if (data.equals("3")) {
                                        data = "特殊单注单场限额";
                                    }
                                    if (data.equals("4")) {
                                        data = "特殊vip限额";
                                    }
                                }

                                if (rcsOperationLogHistory.getType().equals("13")) {
                                    // 是否提前结算  1是  其他否
                                    if (data.equals("1")) {
                                        data = "是";
                                    } else {
                                        data = "否";
                                    }
                                }

                                if (rcsOperationLogHistory.getType().equals("12")) {
                                    // 特殊延时后面的赛种集合
                                    List<Long> sportIdList = JSONObject.parseArray(data, Long.class);
                                    data = getName(sportIdList);
                                }

                                if (rcsOperationLogHistory.getType().equals("16")) {
                                    // 各赛种货量百分比
                                    List<TUserBetRate> sportIdList = JSONObject.parseArray(data, TUserBetRate.class);
                                    StringBuilder name = new StringBuilder();
                                    if (!CollectionUtils.isEmpty(sportIdList)) {
                                        for (TUserBetRate userBetRate : sportIdList) {
                                            String sportName = "";
                                            if (userBetRate.getSportId() == 0) {
                                                sportName = "全部赛种";
                                            } else {
                                                sportName = getSportName(Long.valueOf(userBetRate.getSportId()));
                                            }
                                            name.append(sportName).append(" ");
                                            name.append(userBetRate.getBetRate());
                                            name.append("%   ");
                                        }
                                    }
                                    data = name.toString();
                                }
                            }

                            if (oldData != "") {
                                if (rcsOperationLogHistory.getType().equals("15")) {
                                    //赔率分组
                                    if (oldData.equals("1")) {
                                        oldData = "A";
                                    }
                                    if (oldData.equals("2")) {
                                        oldData = "B";
                                    }
                                    if (oldData.equals("3")) {
                                        oldData = "C";
                                    }
                                    if (oldData.equals("4")) {
                                        oldData = "D";
                                    }
                                    if (oldData.equals("11")) {
                                        oldData = "1";
                                    }
                                    if (oldData.equals("12")) {
                                        oldData = "2";
                                    }
                                    if (oldData.equals("13")) {
                                        oldData = "3";
                                    }
                                    if (oldData.equals("14")) {
                                        oldData = "4";
                                    }
                                    if (oldData.equals("15")) {
                                        oldData = "5";
                                    }
                                }

                                if (rcsOperationLogHistory.getType().equals("11")) {
                                    oldData = oldData + "s";
                                }
                                if (rcsOperationLogHistory.getType().equals("17")) {
                                    // 1无  2特殊百分比限额 3特殊单注单场限额 4特殊vip限额
                                    if (oldData.equals("1")) {
                                        oldData = "无";
                                    }
                                    if (oldData.equals("2")) {
                                        oldData = "特殊百分比限额";
                                    }
                                    if (oldData.equals("3")) {
                                        oldData = "特殊单注单场限额";
                                    }
                                    if (oldData.equals("4")) {
                                        oldData = "特殊vip限额";
                                    }
                                }

                                if (rcsOperationLogHistory.getType().equals("13")) {
                                    // 是否提前结算  1是  其他否
                                    if (oldData.equals("1")) {
                                        oldData = "是";
                                    } else {
                                        oldData = "否";
                                    }
                                }

                                if (rcsOperationLogHistory.getType().equals("12")) {
                                    // 特殊延时后面的赛种集合
                                    List<Long> sportIdList = JSONObject.parseArray(oldData, Long.class);
                                    oldData = getName(sportIdList);
                                }

                                if (rcsOperationLogHistory.getType().equals("16")) {
                                    // 各赛种货量百分比
                                    List<TUserBetRate> sportIdList = JSONObject.parseArray(oldData, TUserBetRate.class);
                                    StringBuilder name = new StringBuilder();
                                    if (!CollectionUtils.isEmpty(sportIdList)) {
                                        for (TUserBetRate userBetRate : sportIdList) {
                                            String sportName = "";
                                            if (userBetRate.getSportId() == 0) {
                                                sportName = "全部赛种";
                                            } else {
                                                sportName = getSportName(Long.valueOf(userBetRate.getSportId()));
                                            }
                                            name.append(sportName).append(" ");
                                            name.append(userBetRate.getBetRate());
                                            name.append("% ");
                                        }
                                    }
                                    oldData = name.toString();
                                }
                            }
                            stringBuilder.append(logData.getName()).append(":").append(oldData).append("---->").append(data);
                        }
                    }
                }

                if (rcsOperationLogHistory.getType().equals("2") || rcsOperationLogHistory.getType().equals("11") || rcsOperationLogHistory.getType().equals("12")) {
                    rcsOperationLogHistory.setType("特殊延时");
                } else if (rcsOperationLogHistory.getType().equals("3") || rcsOperationLogHistory.getType().equals("20") || rcsOperationLogHistory.getType().equals("21") || rcsOperationLogHistory.getType().equals("22") || rcsOperationLogHistory.getType().equals("14") || rcsOperationLogHistory.getType().equals("17") || rcsOperationLogHistory.getType().equals("18")) {
                    rcsOperationLogHistory.setType("特殊限额");
                } else if (rcsOperationLogHistory.getType().equals("15")) {
                    rcsOperationLogHistory.setType("赔率分组");
                } else if (rcsOperationLogHistory.getType().equals("16")) {
                    rcsOperationLogHistory.setType("货量百分比");
                } else if (rcsOperationLogHistory.getType().equals("13")) {
                    rcsOperationLogHistory.setType("提前结算");
                }

                rcsOperationLogHistory.setUpdateContent(stringBuilder.toString());
            }
        }
        return rcsOperationLogHistorieList;
    }
    /**
     * 业务日志代码处理
     * */
    private String setBusinessTypeName(String value){
        String levelName = "";
        switch (value){
            case "10010":
                levelName = "商户管理";
                break;
            case "10020":
                levelName = "标签管理";
                break;
            case "10030":
                levelName = "投注特征标签风控措施";
                break;
            case "10040":
                levelName = "动态风控设置";
                break;
            case "10050":
                levelName = "危险联赛池管理";
                break;
            case "10060":
                levelName = "危险球队池管理";
                break;
            default:
                levelName=value;
                break;
        }
        return levelName;
    }
    private String getName(List<Long> sportIdList) {
        StringBuilder name = new StringBuilder();
        if (!CollectionUtils.isEmpty(sportIdList)) {
            int size = sportIdList.size();
            for (int x = 0; x < size; x++) {
                String sportName = getSportName(sportIdList.get(x));
                if (sportName == null || "null".equals(sportName)) {
                    continue;
                }
                if (x == size - 1) {
                    name.append(getSportName(sportIdList.get(x)));
                } else {
                    name.append(getSportName(sportIdList.get(x))).append(" ");
                }
            }
        }
        return name.toString();
    }

    private String getSportName(Long sportId) {
        if (CollectionUtils.isEmpty(nameHashMap)) {
            List<StandardSportType> standardSportTypeList = standardSportTypeService.getStandardSportTypeList();
            for (StandardSportType standardSportType : standardSportTypeList) {
                nameHashMap.put(standardSportType.getNameCode(), standardSportType.getIntroduction());
            }
        }
        return nameHashMap.get(sportId);
    }

    private Boolean equels(String oldData, String data) {
        if (oldData == null) {
            if (data == null || data.length() == 0) {
                return true;
            }
            return false;
        } else {
            return oldData.equals(data);
        }
    }
}
