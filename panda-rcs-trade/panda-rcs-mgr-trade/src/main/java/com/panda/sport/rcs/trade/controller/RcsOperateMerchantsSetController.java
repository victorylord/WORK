package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.mapper.RcsOperateMerchantsSetMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsOperateMerchantsSet;
import com.panda.sport.rcs.pojo.vo.RcsOperateMerchantsSetVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.dto.RcsOperateMerchantsSetDTO;
import com.panda.sport.rcs.trade.wrapper.RcsOperateMerchantsSetService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: xindaima
 * @description: 操盘商户设置
 * @author: kimi
 * @create: 2020-12-02 14:03
 **/
@RestController
@RequestMapping("/rcsOperateMerchantsSet")
@Slf4j
public class RcsOperateMerchantsSetController {
    @Autowired
    private RcsOperateMerchantsSetMapper rcsOperateMerchantsSetMapper;
    @Autowired
    private RcsOperateMerchantsSetService rcsOperateMerchantsSetService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public HttpResponse<List<RcsOperateMerchantsSetVo>> getList() {
        try {
            List<RcsOperateMerchantsSetVo> rcsOperateMerchantsSets = rcsOperateMerchantsSetMapper.selectRcsOperateMerchantsSet();
            return HttpResponse.success(rcsOperateMerchantsSets);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public HttpResponse update(@RequestBody RcsOperateMerchantsSetDTO rcsOperateMerchantsSetDTO) {
        try {
            List<RcsOperateMerchantsSet> rcsOperateMerchantsSetList = rcsOperateMerchantsSetDTO.getMerchantIdList();
            if(rcsOperateMerchantsSetList.isEmpty()){
                return HttpResponse.failToMsg("修改参数不能为空");
            }
            ExecutorService executorService = Executors.newCachedThreadPool();
            rcsOperateMerchantsSetList.forEach(s -> {
                executorService.execute(() -> {
                    rcsOperateMerchantsSetService.update(s, new QueryWrapper<RcsOperateMerchantsSet>().lambda().eq(RcsOperateMerchantsSet::getMerchantsId, s.getId()));
                    producerSendMessageUtils.sendMessage("RCS_OPERATE_MERCHANTS_SET", null,s.getId().toString() ,s.getId().toString());
                });
            });
            executorService.shutdown();
            redisClient.delete("rsisk:trade:merchants:status");
            return HttpResponse.success();
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public HttpResponse<List<RcsOperateMerchantsSet>> selectAllMerchants() {
        try {
            List<RcsOperateMerchantsSet> rcsOperateMerchantsSets = rcsOperateMerchantsSetMapper.selectAllMerchants();
            return HttpResponse.success(rcsOperateMerchantsSets);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }
}
