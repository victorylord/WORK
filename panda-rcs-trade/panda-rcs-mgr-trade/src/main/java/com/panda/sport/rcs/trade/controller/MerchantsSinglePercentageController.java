package com.panda.sport.rcs.trade.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.mapper.MerchantsSinglePercentageMapper;
import com.panda.sport.rcs.mapper.RcsOperateMerchantsSetMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.pojo.MerchantsSinglePercentage;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsOperateMerchantsSet;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MerchantMatchLimitReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 商户单场限额监控表 前端控制器
 * </p>
 *
 * @author lithan
 * @since 2021-11-24
 */
@RestController
@RequestMapping("/merchantsSinglePercentage")
@Slf4j
public class MerchantsSinglePercentageController {

    @Autowired
    MerchantsSinglePercentageMapper merchantsSinglePercentageMapper;

    @Autowired
    RcsOperateMerchantsSetMapper rcsOperateMerchantsSetMapper;

    @Autowired
    StandardMatchInfoMapper standardMatchInfoMapper;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    private HttpResponse getList(@RequestBody MerchantMatchLimitReqVo reqVo) {
        try {
            //赛事结束的过滤一波
            List<Long> list = merchantsSinglePercentageMapper.list();
            if (ObjectUtils.isNotEmpty(list)) {
                MerchantsSinglePercentage entity = new MerchantsSinglePercentage();
                entity.setStatus(0);
                LambdaUpdateWrapper<MerchantsSinglePercentage> merchantsSingleWrapper = new LambdaUpdateWrapper();
                merchantsSingleWrapper.in(MerchantsSinglePercentage::getMatchId,list);
                merchantsSinglePercentageMapper.update(entity, merchantsSingleWrapper);
            }
            log.info("::{}::赛事结束的过滤:" + list, CommonUtil.getRequestId());

            List<Long> mathcIds = new ArrayList<>();
            if(ObjectUtils.isNotEmpty(reqVo.getMatchIds())){
                for (String matchId : reqVo.getMatchIds()) {
                    mathcIds.add(Long.valueOf(matchId) % 10000000);
                }
            }
            LambdaQueryWrapper<MerchantsSinglePercentage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(ObjectUtils.isNotEmpty(reqVo.getMerchant()), MerchantsSinglePercentage::getMerchantsId, reqVo.getMerchant());
            lambdaQueryWrapper.in(ObjectUtils.isNotEmpty(reqVo.getMatchIds()), MerchantsSinglePercentage::getMatchId, mathcIds);
            lambdaQueryWrapper.ge(ObjectUtils.isNotEmpty(reqVo.getPercentage()), MerchantsSinglePercentage::getPercentage, reqVo.getPercentage());
            lambdaQueryWrapper.eq(ObjectUtils.isNotEmpty(reqVo.getPercentage()), MerchantsSinglePercentage::getStatus, 1);
            //排序
            if (reqVo.getOrderType() == 0) {
                if (reqVo.getOrderColumn() == 0) {
                    lambdaQueryWrapper.orderByAsc(MerchantsSinglePercentage::getPercentage);
                }
                if (reqVo.getOrderColumn() == 1) {
                    lambdaQueryWrapper.orderByAsc(MerchantsSinglePercentage::getMerchantsId);
                }
                if (reqVo.getOrderColumn() == 2) {
                    lambdaQueryWrapper.orderByAsc(MerchantsSinglePercentage::getMatchId);
                }
            }
            if (reqVo.getOrderType() == 1) {
                if (reqVo.getOrderColumn() == 0) {
                    lambdaQueryWrapper.orderByDesc(MerchantsSinglePercentage::getPercentage);
                }
                if (reqVo.getOrderColumn() == 1) {
                    lambdaQueryWrapper.orderByDesc(MerchantsSinglePercentage::getMerchantsId);
                }
                if (reqVo.getOrderColumn() == 2) {
                    lambdaQueryWrapper.orderByDesc(MerchantsSinglePercentage::getMatchId);
                }
            }
            Page<MerchantsSinglePercentage> page = new Page<>(reqVo.getPage(), reqVo.getPageSize());
            IPage<MerchantsSinglePercentage> iPage = merchantsSinglePercentageMapper.selectPage(page, lambdaQueryWrapper);
            for (MerchantsSinglePercentage record : iPage.getRecords()) {
                LambdaQueryWrapper<RcsOperateMerchantsSet> wrapper = new LambdaQueryWrapper<RcsOperateMerchantsSet>();
                wrapper.eq(RcsOperateMerchantsSet::getMerchantsId, record.getMerchantsId());
                RcsOperateMerchantsSet merchantsSet = rcsOperateMerchantsSetMapper.selectOne(wrapper);
                record.setMerchantsName(merchantsSet.getMerchantsCode());

                StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(record.getMatchId());
                if (standardMatchInfo != null) {
                    record.setMatchManageId(standardMatchInfo.getMatchManageId());
                }
            }
            log.info("::{}::商户单场限额监控查询:" + JSONObject.toJSONString(iPage),CommonUtil.getRequestId());
            return HttpResponse.success(iPage);
        } catch (Exception e) {
            log.error("::{}::商户单场限额监控查询异常{}:{}",CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("商户单场限额监控查询异常:{}" + e.getMessage());
        }
    }

}
