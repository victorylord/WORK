package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsLabelLimitConfigMapper;
import com.panda.sport.rcs.mapper.RcsSysUserMapper;
import com.panda.sport.rcs.mapper.TUserLevelMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsLabelLimitConfig;
import com.panda.sport.rcs.pojo.RcsLabelSportVolumePercentage;
import com.panda.sport.rcs.pojo.RcsQuotaBusinessLimitLog;
import com.panda.sport.rcs.pojo.TUserLevel;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.ThreadUtil;
import com.panda.sport.rcs.trade.vo.BusinessLogVo;
import com.panda.sport.rcs.trade.wrapper.IRcsLabelSportVolumePercentageService;
import com.panda.sport.rcs.trade.wrapper.RcsLabelLimitConfigService;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsLabelLimitConfigVo;
import com.panda.sport.rcs.vo.RcsLabelSportVolumePercentageVo;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-04 12:28
 **/
@Service
public class RcsLabelLimitConfigServiceImpl extends ServiceImpl<RcsLabelLimitConfigMapper, RcsLabelLimitConfig> implements RcsLabelLimitConfigService {
    @Autowired
    private RcsLabelLimitConfigMapper rcsLabelLimitConfigMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    IRcsLabelSportVolumePercentageService rcsLabelSportVolumePercentageService;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private TradeVerificationService tradeVerificationService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private TUserLevelMapper tUserLevelMapper;

    @Autowired
    RcsSysUserMapper rcsSysUserMapper;

    private final static String RCS_FEATURE_LABEL_CONFIG_TOP = "rcs_feature_label_config";

    @Transactional
    @Override
    public HttpResponse updateRcsLabelLimitConfigVo(List<RcsLabelLimitConfigVo> rcsLabelLimitConfigVoList, Integer tradeId) {
        List<RcsLabelLimitConfig> rcsLabelLimitConfigList = new ArrayList<>();
        List<Integer> idList = new ArrayList<>();
        List<RcsLabelSportVolumePercentage> oldVolumePercentages=new ArrayList<>();
        List<RcsLabelSportVolumePercentage> newVolumePercentages=new ArrayList<>();
        try{
        for (RcsLabelLimitConfigVo rcsLabelLimitConfigVo : rcsLabelLimitConfigVoList) {
            //提前结算extraMargin 校验
            checkExtraMargin(rcsLabelLimitConfigVo);
            idList.add(rcsLabelLimitConfigVo.getTagId());
            List<Integer> sportIdList = rcsLabelLimitConfigVo.getSportIdList();
            BigDecimal volumePercentage = rcsLabelLimitConfigVo.getVolumePercentage();
            BigDecimal limitPercentage = rcsLabelLimitConfigVo.getLimitPercentage();
            String key=String.format("rcs:label:order:delay:config:%s", rcsLabelLimitConfigVo.getTagId());
            redisClient.delete(key);
            //发送到接距服务做刷新
            JSONObject json = new JSONObject();
            json.put("key", key);
            json.put("value", "1");
            producerSendMessageUtils.sendMessage("rcs_order_reject_cache_update", "", key, json);
            if (!CollectionUtils.isEmpty(sportIdList)) {
                for (Integer sportId : sportIdList) {
                    RcsLabelLimitConfig rcsLabelLimitConfig = new RcsLabelLimitConfig();
                    BeanCopyUtils.copyProperties(rcsLabelLimitConfigVo, rcsLabelLimitConfig);
                    rcsLabelLimitConfig.setUpdateUserId(tradeId);
                    rcsLabelLimitConfig.setId(null);
                    rcsLabelLimitConfig.setSportId(sportId);
                    rcsLabelLimitConfig.setVolumePercentage(volumePercentage.divide(new BigDecimal(100)));
                    rcsLabelLimitConfigList.add(rcsLabelLimitConfig);
                    redisClient.hSet(String.format("rcs:label:order:delay:config:%s", rcsLabelLimitConfigVo.getTagId()), String.valueOf(sportId), JSONObject.toJSONString(rcsLabelLimitConfig));
                }
            } else {
                RcsLabelLimitConfig rcsLabelLimitConfig = new RcsLabelLimitConfig();
                BeanCopyUtils.copyProperties(rcsLabelLimitConfigVo, rcsLabelLimitConfig);
                rcsLabelLimitConfig.setUpdateUserId(tradeId);
                rcsLabelLimitConfig.setId(null);
                rcsLabelLimitConfig.setSportId(null);
                rcsLabelLimitConfig.setVolumePercentage(volumePercentage.divide(new BigDecimal(100)));
                rcsLabelLimitConfigList.add(rcsLabelLimitConfig);
            }
            redisClient.delete("risk:trade:rcs_user_special_tag_limit_config:" + rcsLabelLimitConfigVo.getTagId());
            if (limitPercentage != null && Objects.nonNull(rcsLabelLimitConfigVo.getSpecialBettingLimit()) && 1 == rcsLabelLimitConfigVo.getSpecialBettingLimit()) {
                double v = limitPercentage.doubleValue();
                redisClient.hSet("risk:trade:rcs_user_special_tag_limit_config:" + rcsLabelLimitConfigVo.getTagId(), "percentage", String.valueOf(v / 100));
            }
            redisClient.delete("risk:trade:rcs_user_tag_bet_amount_config:" + rcsLabelLimitConfigVo.getTagId());
            if (volumePercentage != null) {
                double v = volumePercentage.doubleValue();
                redisClient.hSet("risk:trade:rcs_user_tag_bet_amount_config:" + rcsLabelLimitConfigVo.getTagId(), "percentage", String.valueOf(v / 100));
            }

            // 标签赛种货量入库
            List<RcsLabelSportVolumePercentageVo> volumePercentageVoList = rcsLabelLimitConfigVo.getSportVolumePercentageList();
            if (volumePercentageVoList == null) {
                volumePercentageVoList = new ArrayList<>();
            }
            //删除元配置
            LambdaQueryWrapper<RcsLabelSportVolumePercentage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RcsLabelSportVolumePercentage::getTagId, rcsLabelLimitConfigVo.getTagId());
            List<RcsLabelSportVolumePercentage> volumePercentages = rcsLabelSportVolumePercentageService.list(wrapper);
            if (!CollectionUtils.isEmpty(volumePercentages)) {
                rcsLabelSportVolumePercentageService.remove(wrapper);
                String userTagLevelKeyOld = "risk:user:tag:level:" + rcsLabelLimitConfigVo.getTagId();
                //删除缓存
                volumePercentages.forEach(e -> {
                    oldVolumePercentages.add(e);
                    redisClient.delete(userTagLevelKeyOld + e.getSportId());
                });
            }
            //插入新配置
            List<RcsLabelSportVolumePercentage> volumePercentageList = new ArrayList<>();
            for (RcsLabelSportVolumePercentageVo volumePercentageVo : volumePercentageVoList) {
                RcsLabelSportVolumePercentage rcsLabelSportVolumePercentage = new RcsLabelSportVolumePercentage();
                BeanUtils.copyProperties(volumePercentageVo, rcsLabelSportVolumePercentage);
                volumePercentageList.add(rcsLabelSportVolumePercentage);
                RcsLabelSportVolumePercentage labelSportVolumePercentage= newVolumePercentages.stream().filter(v->v.getSportId().equals(rcsLabelSportVolumePercentage.getSportId()) && v.getTagId().equals(rcsLabelSportVolumePercentage.getTagId())).findFirst().orElse(null);
                if(Objects.isNull(labelSportVolumePercentage)){
                    newVolumePercentages.add(rcsLabelSportVolumePercentage);
                }
                //标签赛事货量百分比redis缓存设置
                redisClient.hSet("risk:trade:rcs_label_sport_volume_percentage", volumePercentageVo.getTagId() + "_" + volumePercentageVo.getSportId(), volumePercentageVo.getVolumePercentage().toString());
                //缓存标签赛种货量百分比
                String userTagLevelKey = String.format("risk:user:tag:level:%s", volumePercentageVo.getTagId() + "" + volumePercentageVo.getSportId());
                redisClient.setExpiry(userTagLevelKey, JSONObject.toJSONString(rcsLabelSportVolumePercentage), 30 * 60L);
            }
            rcsLabelSportVolumePercentageService.saveBatch(volumePercentageList);
        }
        QueryWrapper<TUserLevel> queryWrapper = new QueryWrapper();
        List<TUserLevel> tUserLevels = tUserLevelMapper.selectList(queryWrapper);
        List<RcsLabelLimitConfig> oldLabelLimitConfigs =  rcsLabelLimitConfigMapper.getRcsLabelLimitConfigs();
        if (!CollectionUtils.isEmpty(idList)) {
            rcsLabelLimitConfigMapper.removeRcsLabelLimitConfigs(idList);
        }
        if (!CollectionUtils.isEmpty(rcsLabelLimitConfigList)) {
            saveBatch(rcsLabelLimitConfigList);
        }
        BusinessLogVo businessLogVo=new BusinessLogVo();
        businessLogVo.setOldLabelLimitConfigs(oldLabelLimitConfigs);
        businessLogVo.setNewsLabelLimitConfigs(rcsLabelLimitConfigVoList);
        businessLogVo.setOldLabelSportVolumePercentages(oldVolumePercentages);
        businessLogVo.setUserId(tradeId.toString());
        businessLogVo.setTUserLevels(tUserLevels);
        List<RcsQuotaBusinessLimitLog> listFuture = taskExecutor.submit(new BusinessLogServiceImpl(businessLogVo)).get();
        if(Objects.nonNull(listFuture)){
            String arrString = JSONArray.toJSONString(listFuture);
            producerSendMessageUtils.sendMessage(CommonUtil.RCS_BUSINESS_LOG_SAVE,null,CommonUtil.logCode,arrString);
        }
        //当货量百分比时，在标签列表中不展示
        List<RcsLabelLimitConfigVo> collect = rcsLabelLimitConfigVoList.stream().filter(bean -> bean.getVolumePercentage().equals(BigDecimal.ZERO)).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(collect)) {
            redisClient.set("rcs:user:monitor:currentTimeOrder:tagList", "");
        } else {
            String value = "\"";
            for (RcsLabelLimitConfigVo rcsLabelLimitConfigVo : collect) {
                value = value + rcsLabelLimitConfigVo.getTagId() + ",";
            }
            value = value.substring(0, value.length() - 1);
            value = value + "\"";
            redisClient.set("rcs:user:monitor:currentTimeOrder:tagList", value);
        }

        producerSendMessageUtils.sendMessage(RCS_FEATURE_LABEL_CONFIG_TOP, RCS_FEATURE_LABEL_CONFIG_TOP, tradeVerificationService.getRequestId(), rcsLabelLimitConfigVoList);
        return HttpResponse.success();
        }catch (Exception ex){
            log.error("操作失败",ex);
            return HttpResponse.failToMsg("操作失败");
        }
    }

    /**
     * @return void
     * @Description //校验rcsLabelLimitConfigVo
     * 只能输入1位小数，且小数位只能为0或者5，且输入数值绝对值不能大于4。
     * @Param [rcsLabelLimitConfigVo]
     * @Author sean
     * @Date 2022/4/8
     **/
    private void checkExtraMargin(RcsLabelLimitConfigVo rcsLabelLimitConfigVo) {
        BigDecimal extraMargin = rcsLabelLimitConfigVo.getExtraMargin();
        if (ObjectUtils.isEmpty(extraMargin)) {
            return;
        }
        if (extraMargin.abs().compareTo(BigDecimal.valueOf(20)) >= 1) {
            throw new RcsServiceException("输入数值绝对值不能大于20");
        }
        if (extraMargin.divideAndRemainder(new BigDecimal("0.5"))[1].compareTo(BigDecimal.ZERO) > 0) {
            throw new RcsServiceException("只能输入1位小数，且小数位只能为0或者5");
        }
    }
}
