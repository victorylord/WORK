package com.panda.sport.rcs.trade.controller;


import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.pojo.RiskMerchantManager;
import com.panda.sport.rcs.pojo.StandardSportType;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerStatusEnum;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerTypeEnum;
import com.panda.sport.rcs.trade.service.IRiskMerchantManagerService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.EasyExcelUtils;
import com.panda.sport.rcs.trade.vo.RiskMerchantManagerExcelVo;
import com.panda.sport.rcs.trade.wrapper.IStandardSportTypeService;
import com.panda.sport.rcs.vo.*;
import com.panda.sport.rcs.vo.riskmerchantmanager.UserChangeTagVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 商户管控记录表 前端控制器
 * </p>
 *
 * @author lithan
 * @since 2022-03-27
 */
@RestController
@RequestMapping("/riskMerchantManager")
@Slf4j
public class RiskMerchantManagerController {
    @Resource
    private IRiskMerchantManagerService riskMerchantManagerService;
    @Autowired
    private IStandardSportTypeService sportTypeService;

    @PostMapping(value = "/changeUserTag")
    public HttpResponse changeUserTag(@RequestBody UserChangeTagVo vo) {
        try {
            riskMerchantManagerService.changeUserTag(vo);
            return HttpResponse.success();
        } catch (Exception e) {
            log.info("::{}::更新用户标签异常:{}:{}",CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("更新用户标签失败:" + e.getMessage());
        }
    }


    @ApiOperation(value = "待处理count", notes = "待处理count")
    @PostMapping("/pendingCount")
    public HttpResponse<Long> pendingCount(@RequestBody RiskMerchantManagerQueryVo param) {
        try {
            return HttpResponse.success(riskMerchantManagerService.count(new QueryWrapper<RiskMerchantManager>().lambda().eq(RiskMerchantManager::getStatus, RiskMerchantManagerStatusEnum.Type_0.getCode())
                    .eq(RiskMerchantManager::getMerchantCode, param.getMerchantCode())));
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, "待处理count，查询异常：" + e.getMessage());
        }
    }

    /**
     * 商户管控分页数据
     *
     * @return
     */
    @ApiOperation(value = "商户管控记录分页接口", notes = "商户管控记录分页接口")
    @PostMapping("/pageList")
    public HttpResponse<IPage<RiskMerchantManagerVo>> listStandardMarketSellVo(@RequestBody RiskMerchantManagerQueryVo param) {
        try {
            IPage<RiskMerchantManagerVo> rtnList = riskMerchantManagerService.pageList(param);
            return HttpResponse.success(rtnList);
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, "商户管控记录分页接口，查询异常：" + e.getMessage());
        }
    }

    /**
     * 更新状态
     *
     * @return
     */
    @ApiOperation(value = "更新状态", notes = "更新状态 ")
    @PostMapping("/updateStatus")
    public HttpResponse<Boolean> listStandardMarketSellVo(@RequestBody @Validated RiskMerchantManagerUpdateVo param) {
        try {
            return HttpResponse.success(riskMerchantManagerService.updateStatus(param) > 0);
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, "更新状态，异常：" + e.getMessage());
        }
    }


    /**
     * 商户管控分页数据
     *
     * @return
     */
    @ApiOperation(value = "商户管控记录表导出（外部使用）", notes = "商户管控记录表导出（外部使用 ")
    @PostMapping("/exportOutPutList")
    public HttpResponse<List<RiskMerchantManagerVo>> listRiskMerchantManagerVoExport(@RequestBody RiskMerchantManagerQueryVo param) {
        try {
            log.info("::{}::商户管控记录表导出（外部使用）,参数:{}",CommonUtil.getRequestId(), param);
            List<RiskMerchantManager> riskMerchantManagerList = riskMerchantManagerService.list(param);
            List<RiskMerchantManagerVo> list = new ArrayList<>();
            if (!CollectionUtils.isEmpty(riskMerchantManagerList)) {
                List<StandardSportType> sportTypeList = sportTypeService.list();
                riskMerchantManagerList.forEach(obj -> {
                    RiskMerchantManagerVo vo = new RiskMerchantManagerVo();
                    BeanUtils.copyProperties(obj, vo);
                    vo.setUserId(String.valueOf(obj.getUserId()));
                    vo.setRecommendValue(riskMerchantManagerService.getRecommendValue(vo.getType(), vo.getRecommendValue(), sportTypeList));
                    list.add(vo);
                });
            }
            return HttpResponse.success(list);
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, "商户管控记录表导出-外部使用，查询异常：" + e.getMessage());
        }
    }




    @ApiOperation(value = "商户管控记录表批量导入", notes = "商户管控记录表导出 ")
    @PostMapping("/importExcel")
    public HttpResponse<Boolean> importBatchUpdateStatus(@RequestParam("file") MultipartFile file) {
        try {
            //使用huTool工具类导入Excel文件
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<RiskMerchantImportUpdateVo> listData = reader.readAll(RiskMerchantImportUpdateVo.class);
            if (CollectionUtils.isEmpty(listData)) {
				log.error("::{}::importBatchUpdateStatus 导入文件数据为空！", CommonUtil.getRequestId());
                return HttpResponse.success(false);
            }
            //批量更新
            return HttpResponse.success(riskMerchantManagerService.batchUpdateStatus(listData));
        } catch (IOException e) {
			log.error("::{}::importBatchUpdateStatus 商户管控记录表导入Excel异常：{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(false);
    }

    @ApiOperation(value = "商户管控记录表批量导入(外部使用)", notes = "商户管控记录表导出(外部使用) ")
    @PostMapping("/batchUpdateStatus")
    public HttpResponse<Boolean> batchUpdateStatus(@RequestBody List<RiskMerchantImportUpdateVo> voList) {
        try {
            if (CollectionUtils.isEmpty(voList)) {
                log.error("::{}::batchUpdateStatus 导入文件数据为空！",CommonUtil.getRequestId());
                return HttpResponse.success(false);
            }
            //批量更新
            return HttpResponse.success(riskMerchantManagerService.batchUpdateStatus(voList));
        } catch (Exception e) {
			log.error("::{}::batchUpdateStatus 商户管控记录表导入Excel异常：{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(false);
    }





}
