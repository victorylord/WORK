package com.panda.sport.rcs.trade.wrapper.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.panda.sport.data.rcs.dto.limit.enums.LimitDataTypeEnum;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.mapper.RcsUserConfigMapper;
import com.panda.sport.rcs.mapper.RcsUserSpecialBetLimitConfigMapper;
import com.panda.sport.rcs.mapper.TUserBetRateMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.vo.LogData;
import com.panda.sport.rcs.trade.enums.RiskMerchantManagerStatusEnum;
import com.panda.sport.rcs.trade.enums.SpecialBettingLimitTypeEnum;
import com.panda.sport.rcs.trade.enums.UserLogTypeEnum;
import com.panda.sport.rcs.trade.service.IRiskMerchantManagerService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.*;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.UserReferenceLimitVo;
import com.panda.sports.api.vo.ShortSysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-12-08 18:16
 **/
@Slf4j
@Service
public class RcsUserConfigServiceImpl extends ServiceImpl<RcsUserConfigMapper, RcsUserConfig> implements RcsUserConfigService {
    @Autowired
    private IRcsUserConfigNewService rcsUserConfigNewService;
    @Autowired
    private RcsUserSpecialBetLimitConfigMapper rcsUserSpecialBetLimitConfigMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RcsOperationLogMapper rcsOperationLogMapper;
    @Autowired
    private RcsTradeRestrictMerchantSettingService rcsTradeRestrictMerchantSettingService;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;
    @Autowired
    private TUserBetRateMapper userBetRateMapper;
    @Autowired
    private IRiskMerchantManagerService riskMerchantManagerService;

    /**
     * 限额缓存
     */
    private String hkey = "risk:trade:rcs_user_special_bet_limit_config:%s";
    /**
     * 类型
     */
    private String Hkey1 = "type";
    /**
     * 特殊货量
     */
    private String Hkey4 = "specialQuantityPercentage";
    /**
     * 单注赔付限额
     */
    private String Hkey2 = "%s_%s_single_note_claim_limit";
    /**
     * 单场
     */
    private String Hkey3 = "%s_%s_single_game_claim_limit";
    /**
     * 冠军玩法限额比例
     */
    private static final String CHAMPION_LIMIT_RATE = "championLimitRate";

    /**
     * 延迟数据干掉
     */
    private String SPECIAL_USER_CONFIG = "rcs:special:user:order:delay:config:%s";
    private static String USER_LABEL_CONFIG = "rcs:special:user:order:delay:sencond:config:%s";
    @Autowired
    private IStandardSportTypeService standardSportTypeService;
    private HashMap<Long, String> nameHashMap = new HashMap<>();

    @Override
    public RcsUserSpecialBetLimitConfigVo getList(Long userId) {
        List<Long> userIdList = new ArrayList<>();
        userIdList.add(userId);
        HashMap<Long, RcsUserConfigVo> rcsUserConfigVo = getRcsUserConfigVo(userIdList);
        RcsUserSpecialBetLimitConfigVo rcsUserSpecialBetLimitConfigVo = new RcsUserSpecialBetLimitConfigVo();
        rcsUserSpecialBetLimitConfigVo.setRcsUserConfigVo(rcsUserConfigVo.get(userId));
        //下面的数据
        QueryWrapper<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigQueryWrapper = new QueryWrapper<>();
        rcsUserSpecialBetLimitConfigQueryWrapper.lambda().eq(RcsUserSpecialBetLimitConfig::getUserId, userId);
        List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList = rcsUserSpecialBetLimitConfigMapper.selectList(rcsUserSpecialBetLimitConfigQueryWrapper);
        //投注特殊限额需要特殊处理
        boolean isHaveSpecialBetting = false;
        HashMap<Integer, List<RcsUserSpecialBetLimitConfig>> rcsUserSpecialBetLimitConfigHashMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList)) {
            for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList) {
                List<RcsUserSpecialBetLimitConfig> limitConfigList = rcsUserSpecialBetLimitConfigHashMap.get(rcsUserSpecialBetLimitConfig.getSpecialBettingLimitType());
                if (SpecialBettingLimitTypeEnum.SINGLE_GAME_QUOTA.getType().equals(rcsUserSpecialBetLimitConfig.getSpecialBettingLimitType())) {
                    isHaveSpecialBetting = true;
                }
                if (CollectionUtils.isEmpty(limitConfigList)) {
                    limitConfigList = new ArrayList<>();
                    rcsUserSpecialBetLimitConfigHashMap.put(rcsUserSpecialBetLimitConfig.getSpecialBettingLimitType(), limitConfigList);
                }
                rcsUserSpecialBetLimitConfig.setOldPercentageLimit(rcsUserSpecialBetLimitConfig.getPercentageLimit());
                rcsUserSpecialBetLimitConfig.setOldSingleGameClaimLimit(rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit());
                rcsUserSpecialBetLimitConfig.setOldSingleNoteClaimLimit(rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit());
                limitConfigList.add(rcsUserSpecialBetLimitConfig);
            }
        }
        if (!isHaveSpecialBetting) {
            List<RcsUserSpecialBetLimitConfig> init = init(userId);
            rcsUserSpecialBetLimitConfigHashMap.put(SpecialBettingLimitTypeEnum.SINGLE_GAME_QUOTA.getType(), init);
        }

        List<RcsUserSpecialBetLimitConfigDataVo> rcsUserSpecialBetLimitConfigDataVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigHashMap)) {
            for (Map.Entry<Integer, List<RcsUserSpecialBetLimitConfig>> entry : rcsUserSpecialBetLimitConfigHashMap.entrySet()) {
                RcsUserSpecialBetLimitConfigDataVo rcsUserSpecialBetLimitConfigDataVo = new RcsUserSpecialBetLimitConfigDataVo();
                rcsUserSpecialBetLimitConfigDataVo.setSpecialBettingLimitType(entry.getKey());
                sort(rcsUserSpecialBetLimitConfigDataVo, entry.getValue());
                if (entry.getKey().equals(SpecialBettingLimitTypeEnum.SINGLE_GAME_QUOTA.getType())) {
                    handleSpecilLimit(entry.getValue(), userId);
                }
                rcsUserSpecialBetLimitConfigDataVoList.add(rcsUserSpecialBetLimitConfigDataVo);
            }
        }
        rcsUserSpecialBetLimitConfigVo.setRcsUserSpecialBetLimitConfigDataVoList(rcsUserSpecialBetLimitConfigDataVoList);
        return rcsUserSpecialBetLimitConfigVo;
    }

    public HashMap<Long, RcsUserConfigVo> getRcsUserConfigVo(List<Long> userIdList) {
//        QueryWrapper<RcsUserConfig> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().in(RcsUserConfig::getUserId,userIdList);
//        List<RcsUserConfig> rcsUserConfigs = rcsUserConfigNewService.getByUserId(queryWrapper);
//        HashMap<Long,RcsUserConfigVo> hashMap=new HashMap<>();
//        if (!CollectionUtils.isEmpty(rcsUserConfigs)){
//            for (RcsUserConfig rcsUserConfig:rcsUserConfigs){
//                RcsUserConfigVo rcsUserConfigVo = hashMap.get(rcsUserConfig.getUserId());
//                if (rcsUserConfigVo==null){
//                    rcsUserConfigVo=new RcsUserConfigVo();
//                    rcsUserConfigVo.setSportIdList(new ArrayList<>());
//                    rcsUserConfigVo.setUserId(rcsUserConfig.getUserId());
//                    rcsUserConfigVo.setBetExtraDelay(rcsUserConfig.getBetExtraDelay());
//                    //类型兼容
//                    Integer specialBettingLimit = rcsUserConfig.getSpecialBettingLimit();
//                    if (specialBettingLimit == null || specialBettingLimit == 0){
//                        specialBettingLimit = 1;
//                    }
//                    rcsUserConfigVo.setSpecialBettingLimit(specialBettingLimit);
//                    rcsUserConfigVo.setSpecialVolume(rcsUserConfig.getSpecialVolume());
//                    rcsUserConfigVo.setSettlementInAdvance(rcsUserConfig.getSettlementInAdvance());
//                    rcsUserConfigVo.setRemarks(rcsUserConfig.getRemarks());
//                    rcsUserConfigVo.setUpdateTime(rcsUserConfig.getUpdateTime());
//                    rcsUserConfigVo.setTagMarketLevelId(rcsUserConfig.getTagMarketLevelId());
//                    rcsUserConfigVo.setChampionLimitRate(rcsUserConfig.getChampionLimitRate());
//                    hashMap.put(rcsUserConfig.getUserId(),rcsUserConfigVo);
//                }
//                List<Long> sportId = rcsUserConfigVo.getSportIdList();
//                sportId.add(rcsUserConfig.getSportId());
//            }
//        }
        return new HashMap<>(rcsUserConfigNewService.getByUserIds(userIdList));
    }

    private List<RcsUserSpecialBetLimitConfig> init(Long userId) {
        List<RcsUserSpecialBetLimitConfig> limitConfigs = new ArrayList<>();
        RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig1 = new RcsUserSpecialBetLimitConfig();
        RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig2 = new RcsUserSpecialBetLimitConfig();
        RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig3 = new RcsUserSpecialBetLimitConfig();
        RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig4 = new RcsUserSpecialBetLimitConfig();
        RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig5 = new RcsUserSpecialBetLimitConfig();
        rcsUserSpecialBetLimitConfig1.setUserId(userId);
        rcsUserSpecialBetLimitConfig2.setUserId(userId);
        rcsUserSpecialBetLimitConfig3.setUserId(userId);
        rcsUserSpecialBetLimitConfig4.setUserId(userId);
        rcsUserSpecialBetLimitConfig5.setUserId(userId);
        rcsUserSpecialBetLimitConfig1.setOrderType(1);
        rcsUserSpecialBetLimitConfig2.setOrderType(1);
        rcsUserSpecialBetLimitConfig3.setOrderType(1);
        rcsUserSpecialBetLimitConfig4.setOrderType(1);
        rcsUserSpecialBetLimitConfig5.setOrderType(2);
        rcsUserSpecialBetLimitConfig1.setSportId(1);
        rcsUserSpecialBetLimitConfig2.setSportId(2);
        rcsUserSpecialBetLimitConfig3.setSportId(0);
        rcsUserSpecialBetLimitConfig4.setSportId(-1);
        rcsUserSpecialBetLimitConfig5.setSportId(-1);
        rcsUserSpecialBetLimitConfig1.setStatus(0);
        rcsUserSpecialBetLimitConfig2.setStatus(0);
        rcsUserSpecialBetLimitConfig3.setStatus(0);
        rcsUserSpecialBetLimitConfig4.setStatus(0);
        rcsUserSpecialBetLimitConfig5.setStatus(0);
        rcsUserSpecialBetLimitConfig1.setSpecialBettingLimitType(3);
        rcsUserSpecialBetLimitConfig2.setSpecialBettingLimitType(3);
        rcsUserSpecialBetLimitConfig3.setSpecialBettingLimitType(3);
        rcsUserSpecialBetLimitConfig4.setSpecialBettingLimitType(3);
        rcsUserSpecialBetLimitConfig5.setSpecialBettingLimitType(3);
        limitConfigs.add(rcsUserSpecialBetLimitConfig1);
        limitConfigs.add(rcsUserSpecialBetLimitConfig2);
        limitConfigs.add(rcsUserSpecialBetLimitConfig3);
        limitConfigs.add(rcsUserSpecialBetLimitConfig4);
        limitConfigs.add(rcsUserSpecialBetLimitConfig5);
        return limitConfigs;
    }

    private void sort(RcsUserSpecialBetLimitConfigDataVo rcsUserSpecialBetLimitConfigDataVo, List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList) {
        List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList1 = rcsUserSpecialBetLimitConfigDataVo.getRcsUserSpecialBetLimitConfigList1();
        List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList2 = rcsUserSpecialBetLimitConfigDataVo.getRcsUserSpecialBetLimitConfigList2();
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList)) {
            for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList) {
                if (rcsUserSpecialBetLimitConfig.getOrderType() != null && rcsUserSpecialBetLimitConfig.getOrderType().intValue() == 2) {
                    rcsUserSpecialBetLimitConfigList2.add(rcsUserSpecialBetLimitConfig);
                } else {
                    rcsUserSpecialBetLimitConfigList1.add(rcsUserSpecialBetLimitConfig);
                }
            }
        }
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList1)) {
            Collections.sort(rcsUserSpecialBetLimitConfigList1, new Comparator<RcsUserSpecialBetLimitConfig>() {
                @Override
                public int compare(RcsUserSpecialBetLimitConfig o1, RcsUserSpecialBetLimitConfig o2) {
                    Integer sportId1 = o1.getSportId();
                    Integer sportId2 = o2.getSportId();
                    if (sportId1 != null && sportId1 == 0) {
                        return 1;
                    } else if (sportId2 != null && sportId2 == 0) {
                        return -1;
                    }
                    if (sportId1 != null && sportId2 != null) {
                        if (sportId1 > sportId2) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                    return 0;
                }
            });
        }
    }

    private void handleSpecilLimit(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, Long userId) {
        long singleNoteClaimLimitMax = 0;
        long singleGameClaimLimitMax = 0;
        BigDecimal bigDecimal = rcsUserSpecialBetLimitConfigMapper.selectUserQuotaRatio(userId);
        BigDecimal bigDecimal1 = rcsUserSpecialBetLimitConfigMapper.selectSserQuotaCrossLimit();
        List<UserReferenceLimitVo> userReferenceLimitVos = rcsUserSpecialBetLimitConfigMapper.selectByBasketballAndFootball();
        BigDecimal bigDecimal2 = rcsUserSpecialBetLimitConfigMapper.selectCrossDayCompensation();
        for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList) {
            if (2 == rcsUserSpecialBetLimitConfig.getOrderType()) {
                rcsUserSpecialBetLimitConfig.setSingleNoteClaimLimitMax((long) (bigDecimal1.longValue() * bigDecimal.doubleValue()));
                rcsUserSpecialBetLimitConfig.setSingleGameClaimLimitMax(bigDecimal2.longValue());
                rcsUserSpecialBetLimitConfig.setSportId(-1);
            } else if (1 == rcsUserSpecialBetLimitConfig.getOrderType()) {
                if (1 == rcsUserSpecialBetLimitConfig.getSportId()) {
                    for (UserReferenceLimitVo userReferenceLimitVo : userReferenceLimitVos) {
                        if (userReferenceLimitVo.getSportId().intValue() == 1) {
                            rcsUserSpecialBetLimitConfig.setSingleNoteClaimLimitMax((long) (userReferenceLimitVo.getUserSingleLimit().longValue() * bigDecimal.doubleValue()));
                            rcsUserSpecialBetLimitConfig.setSingleGameClaimLimitMax((long) (userReferenceLimitVo.getUserMatchLimit().longValue() * bigDecimal.doubleValue()));
                            if (rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimitMax() > singleNoteClaimLimitMax) {
                                singleNoteClaimLimitMax = rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimitMax();
                            }
                            if (rcsUserSpecialBetLimitConfig.getSingleGameClaimLimitMax() > singleGameClaimLimitMax) {
                                singleGameClaimLimitMax = rcsUserSpecialBetLimitConfig.getSingleGameClaimLimitMax();
                            }
                            break;
                        }
                    }
                } else if (2 == rcsUserSpecialBetLimitConfig.getSportId()) {
                    for (UserReferenceLimitVo userReferenceLimitVo : userReferenceLimitVos) {
                        if (userReferenceLimitVo.getSportId().intValue() == 2) {
                            rcsUserSpecialBetLimitConfig.setSingleNoteClaimLimitMax((long) (userReferenceLimitVo.getUserSingleLimit() * bigDecimal.doubleValue()));
                            rcsUserSpecialBetLimitConfig.setSingleGameClaimLimitMax((long) (userReferenceLimitVo.getUserMatchLimit() * bigDecimal.doubleValue()));
                            if (rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimitMax() > singleNoteClaimLimitMax) {
                                singleNoteClaimLimitMax = rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimitMax();
                            }
                            if (rcsUserSpecialBetLimitConfig.getSingleGameClaimLimitMax() > singleGameClaimLimitMax) {
                                singleGameClaimLimitMax = rcsUserSpecialBetLimitConfig.getSingleGameClaimLimitMax();
                            }
                            break;
                        }
                    }
                } else if (0 == rcsUserSpecialBetLimitConfig.getSportId()) {
                    rcsUserSpecialBetLimitConfig.setSingleNoteClaimLimitMax((long) (rcsUserSpecialBetLimitConfigMapper.selectSingeOrderByOther().get(0).getUserSingleLimit().longValue() * bigDecimal.doubleValue()));
                    rcsUserSpecialBetLimitConfig.setSingleGameClaimLimitMax((long) (rcsUserSpecialBetLimitConfigMapper.selectSingeMatchByOther().get(0).getUserMatchLimit().longValue() * bigDecimal.doubleValue()));
                }
            }
        }
        setAll(rcsUserSpecialBetLimitConfigList, singleNoteClaimLimitMax, singleGameClaimLimitMax);
    }

    private void setAll(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, long singleNoteClaimLimitMax, long singleGameClaimLimitMax) {
        for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList) {
            if (1 == rcsUserSpecialBetLimitConfig.getOrderType()) {
                if (-1 == rcsUserSpecialBetLimitConfig.getSportId()) {
                    rcsUserSpecialBetLimitConfig.setSingleNoteClaimLimitMax(singleNoteClaimLimitMax);
                    rcsUserSpecialBetLimitConfig.setSingleGameClaimLimitMax(singleGameClaimLimitMax);
                    return;
                }
            }
        }
    }

    @Transactional
    @Override
    public HttpResponse updateRcsUserSpecialBetLimitConfigsVo(RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo, Integer traderId, boolean isTrade) {
        //用于发给业务
        RcsUserSpecialBetLimitConfigsVo oldData = new RcsUserSpecialBetLimitConfigsVo();
        if (!ObjectUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo())) {
            RcsUserConfigVo rcsConfigVo = new RcsUserConfigVo();
            BeanUtils.copyProperties(rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo(), rcsConfigVo);
            oldData.setRcsUserConfigVo(rcsConfigVo);
            if (null == oldData.getRcsUserConfigVo().getBetExtraDelay()) {
                oldData.getRcsUserConfigVo().setBetExtraDelay(-1);
            }
        }
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getRcsUserSpecialBetLimitConfigDataVoList())) {
            oldData.setRcsUserSpecialBetLimitConfigDataVoList(rcsUserSpecialBetLimitConfigsVo.getRcsUserSpecialBetLimitConfigDataVoList());
        }
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getUserBetRateList())) {
            oldData.setUserBetRateList(rcsUserSpecialBetLimitConfigsVo.getUserBetRateList());
        }
        if (!ObjectUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getTraderData())) {
            oldData.setTraderData(rcsUserSpecialBetLimitConfigsVo.getTraderData());
        }
        //外加一个操盘手ID，给商户后台使用
        rcsUserSpecialBetLimitConfigsVo.setTraderId(traderId);
        oldData.setTraderId(traderId);

        String oldStr = JSONObject.toJSONString(oldData);
        log.info("::{}::oldData1:{}", CommonUtil.getRequestId(), oldStr);
        //处理数据
        processingData(rcsUserSpecialBetLimitConfigsVo);

        //kir-1647-特殊限额/特殊管控设置
        RiskMerchantManager riskMerchantManager = new RiskMerchantManager();
        if (!ObjectUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getSubmitType()) && rcsUserSpecialBetLimitConfigsVo.getSubmitType().equals(1)) {
            log.info("::{}::kir-1647-提交商户后台审核:{}", CommonUtil.getRequestId(), JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo));
            processingLog(rcsUserSpecialBetLimitConfigsVo, traderId, 2, riskMerchantManager);
            //需要提交到商户后台审核
            if (!ObjectUtils.isEmpty(riskMerchantManager.getType())) {
                riskMerchantManagerService.initRiskMerchantManager(rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo().getUserId(), riskMerchantManager.getType(), riskMerchantManager.getRecommendValue(), riskMerchantManager.getMerchantShowValue(),
                        rcsUserSpecialBetLimitConfigsVo.getSupplementExplain(), JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo), RiskMerchantManagerStatusEnum.Type_0.getCode());
            }
            return HttpResponse.success();
        } else if (!ObjectUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getSubmitType()) && rcsUserSpecialBetLimitConfigsVo.getSubmitType().equals(2)) {
            log.info("::{}::kir-1647-强制执行:{}", CommonUtil.getRequestId(), JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo));
            String str = JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo);
            RcsUserSpecialBetLimitConfigsVo newVo = JSON.parseObject(str, RcsUserSpecialBetLimitConfigsVo.class);
            processingLog(newVo, traderId, 2, riskMerchantManager);
            //强制执行，不需要审核
            if (!ObjectUtils.isEmpty(riskMerchantManager.getType())) {
                riskMerchantManagerService.initRiskMerchantManager(rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo().getUserId(), riskMerchantManager.getType(), riskMerchantManager.getRecommendValue(), riskMerchantManager.getMerchantShowValue(),
                        rcsUserSpecialBetLimitConfigsVo.getSupplementExplain(), JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo), RiskMerchantManagerStatusEnum.Type_3.getCode());
            }
        } else {
            log.info("::{}::kir-1647-走正常逻辑:{}", CommonUtil.getRequestId(), JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo));
            //否则type=3的时候走正常逻辑
        }
        log.info("::{}::kir-1647-进行入库操作:{}", CommonUtil.getRequestId(), JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo));

        //处理日志
        processingLog(rcsUserSpecialBetLimitConfigsVo, traderId, 1, null);

        //操作缓存
        updateRedis(rcsUserSpecialBetLimitConfigsVo);
        //处理用户风控措施数据（货量百分比）
        userBetRate(rcsUserSpecialBetLimitConfigsVo, traderId);

        //更新数据
        return updateRcsUserSpecialBetLimitConfigData(rcsUserSpecialBetLimitConfigsVo, oldStr, traderId, isTrade);
    }

    /**
     * 处理当前用户货量百分比数据
     *
     * @param rcsUserSpecialBetLimitConfigsVo
     */
    private void userBetRate(RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo, Integer traderId) {
        LambdaQueryWrapper<TUserBetRate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TUserBetRate::getUserId, rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo().getUserId());
        userBetRateMapper.delete(wrapper);

        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getUserBetRateList())) {
            for (TUserBetRate tUserBetRate : rcsUserSpecialBetLimitConfigsVo.getUserBetRateList()) {
                tUserBetRate.setId(null);
                userBetRateMapper.insert(tUserBetRate);
            }
        }
    }

    /**
     * 处理参数status
     *
     * @param rcsUserSpecialBetLimitConfigsVo
     */
    private void processingData(RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo) {
        RcsUserConfigVo rcsUserConfigVo = rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo();
        Integer specialBettingLimit = rcsUserConfigVo.getSpecialBettingLimit();
        List<RcsUserSpecialBetLimitConfigDataVo> rcsUserSpecialBetLimitConfigDataVoList = rcsUserSpecialBetLimitConfigsVo.getRcsUserSpecialBetLimitConfigDataVoList();
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigDataVoList)) {
            RcsUserSpecialBetLimitConfigDataVo rcsUserSpecialBetLimitConfigDataVo = rcsUserSpecialBetLimitConfigDataVoList.get(0);
            if (rcsUserSpecialBetLimitConfigDataVo != null) {
                List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList1 = rcsUserSpecialBetLimitConfigDataVo.getRcsUserSpecialBetLimitConfigList1();
                if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList1)) {
                    if (specialBettingLimit.equals(SpecialBettingLimitTypeEnum.SINGLE_GAME_QUOTA.getType()) || specialBettingLimit.equals(SpecialBettingLimitTypeEnum.VIP_LIMIT.getType())) {
                        int x = 0;
                        for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList1) {
                            if (rcsUserSpecialBetLimitConfig.getSportId() != null && rcsUserSpecialBetLimitConfig.getSportId() == -1 && rcsUserSpecialBetLimitConfig.getOrderType() != null && rcsUserSpecialBetLimitConfig.getOrderType() == 1) {
                                x = rcsUserSpecialBetLimitConfig.getStatus();
                                break;
                            }
                        }
                        for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList1) {
                            if (rcsUserSpecialBetLimitConfig.getSportId() != -1) {
                                rcsUserSpecialBetLimitConfig.setStatus(1 - x);
                            }
                        }
                    } else {
                        for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList1) {
                            //1061需求 由于历史记录会保存两条（包含status=0）的也会保存，所以展示去掉
                            //rcsUserSpecialBetLimitConfig.setStatus(1);
                        }
                    }
                }
                List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList2 = rcsUserSpecialBetLimitConfigDataVo.getRcsUserSpecialBetLimitConfigList2();
                if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList2)) {
                    RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig = rcsUserSpecialBetLimitConfigList2.get(0);
                    rcsUserSpecialBetLimitConfig.setStatus(1);
                }
            }
        }
    }

    private void updateRedis(RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo) {
        //删除限时
        RcsUserConfigVo rcsUserConfigVo = rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo();
        List<RcsUserSpecialBetLimitConfigDataVo> rcsUserSpecialBetLimitConfigDataVoList = rcsUserSpecialBetLimitConfigsVo.getRcsUserSpecialBetLimitConfigDataVoList();
        //redisUtils.del(String.format(SPECIAL_USER_CONFIG, rcsUserConfigVo.getUserId()));
        //redisUtils.del(String.format(USER_LABEL_CONFIG, rcsUserConfigVo.getUserId()));

        Map<String, Object> map = new HashMap<>();
        //更新延迟缓存
        if (Objects.nonNull(rcsUserConfigVo) && !rcsUserConfigVo.getSportIdList().isEmpty()) {
            /*rcsUserConfigVo.getSportIdList().forEach(s -> {

                redisUtils.hset(String.format(USER_LABEL_CONFIG, rcsUserConfigVo.getUserId()), s.toString(), JSONObject.toJSONString(rcsUserConfigVo));
                redisUtils.expire(String.format(USER_LABEL_CONFIG, rcsUserConfigVo.getUserId()), 30, TimeUnit.DAYS);
            });*/
            map.put("rcsUserConfigVo", JSONObject.toJSONString(rcsUserConfigVo));
            map.put("sportIdList", rcsUserConfigVo.getSportIdList());
        }

        //String format = String.format(hkey, String.valueOf(rcsUserConfigVo.getUserId()));
        //更新缓存数据
//        redisUtils.del(format);

        //String betFormat = String.format("risk:trade:rcs_user_sport_type_bet_limit_config:%s", rcsUserConfigVo.getUserId());
        //redisUtils.del(betFormat);
        //1061需求 赛种货量百分比
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigsVo.getUserBetRateList())) {
            /*for (TUserBetRate tUserBetRate : rcsUserSpecialBetLimitConfigsVo.getUserBetRateList()) {
                redisUtils.hset(betFormat, String.valueOf(tUserBetRate.getSportId()), String.valueOf(tUserBetRate.getBetRate()));
            }*/
            map.put("userBetRateList", JSONObject.toJSONString(rcsUserSpecialBetLimitConfigsVo.getUserBetRateList()));
        }

        Integer specialBettingLimit = rcsUserConfigVo.getSpecialBettingLimit();
        //redisUtils.hset(format, Hkey1, String.valueOf(specialBettingLimit));
        map.put("type", specialBettingLimit);

        Integer specialVolume = rcsUserConfigVo.getSpecialVolume();
        if (specialVolume != null) {
            //redisUtils.hset(format, Hkey4, String.valueOf(specialVolume.doubleValue() / 100));
            map.put("specialQuantityPercentage", specialVolume.doubleValue() / 100);
        }
        BigDecimal championLimitRate = rcsUserConfigVo.getChampionLimitRate();
        if (championLimitRate != null) {
            //redisUtils.hset(format, CHAMPION_LIMIT_RATE, championLimitRate.divide(new BigDecimal(100)).toPlainString());
            map.put("championLimitRate", championLimitRate.divide(new BigDecimal(100)));
        }
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigDataVoList) && specialBettingLimit != null) {
            RcsUserSpecialBetLimitConfigDataVo rcsUserSpecialBetLimitConfigDataVo1 = rcsUserSpecialBetLimitConfigDataVoList.get(0);
            List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList1 = rcsUserSpecialBetLimitConfigDataVo1.getRcsUserSpecialBetLimitConfigList1();
            List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList2 = rcsUserSpecialBetLimitConfigDataVo1.getRcsUserSpecialBetLimitConfigList2();
            if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList2)) {
                rcsUserSpecialBetLimitConfigList1.addAll(rcsUserSpecialBetLimitConfigList2);
            }
            JSONArray jsonArray = new JSONArray();
            for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList1) {
                rcsUserSpecialBetLimitConfig.setUserId(rcsUserConfigVo.getUserId());
                rcsUserSpecialBetLimitConfig.setSpecialBettingLimitType(rcsUserConfigVo.getSpecialBettingLimit());
                if (rcsUserSpecialBetLimitConfig.getStatus() == 1) {
                    JSONObject jsonObject = new JSONObject();
                    if (specialBettingLimit.equals(SpecialBettingLimitTypeEnum.PERCENTAGE_LIMIT.getType())) {
                        if (rcsUserSpecialBetLimitConfig.getPercentageLimit() != null) {
                            //redisUtils.hset(format, "percentage", String.valueOf(rcsUserSpecialBetLimitConfig.getPercentageLimit().doubleValue()));
                            jsonObject.put("percentage", rcsUserSpecialBetLimitConfig.getPercentageLimit().doubleValue());
                        }
                    } else if (specialBettingLimit.equals(SpecialBettingLimitTypeEnum.SINGLE_GAME_QUOTA.getType()) || specialBettingLimit.equals(SpecialBettingLimitTypeEnum.VIP_LIMIT.getType())) {
                        jsonObject.put("key1", String.format(Hkey2, rcsUserSpecialBetLimitConfig.getOrderType(), rcsUserSpecialBetLimitConfig.getSportId()));
                        jsonObject.put("singleNoteClaimLimit", rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit());
                        /*if (rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit() != null) {
                            redisUtils.hset(format, String.format(Hkey2, String.valueOf(rcsUserSpecialBetLimitConfig.getOrderType()), String.valueOf(rcsUserSpecialBetLimitConfig.getSportId())), String.valueOf(rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit() * 100));
                        }*/
                        jsonObject.put("key2", String.format(Hkey3, rcsUserSpecialBetLimitConfig.getOrderType(), rcsUserSpecialBetLimitConfig.getSportId()));
                        jsonObject.put("singleGameClaimLimit", rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit());
                        /*if (rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit() != null) {
                            redisUtils.hset(format, String.format(Hkey3, String.valueOf(rcsUserSpecialBetLimitConfig.getOrderType()), String.valueOf(rcsUserSpecialBetLimitConfig.getSportId())), String.valueOf(rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit() * 100));
                        } */
                    }
                    if (!jsonObject.isEmpty()) {
                        jsonArray.add(jsonObject);
                    }
                }
            }
            if (!jsonArray.isEmpty()) {
                map.put("jsonArray", jsonArray);
            }
        }
        map.put("userId", rcsUserConfigVo.getUserId());
        map.put("dataType", 9);
        //通知sdk 清除缓存
        sendMessage.sendMessage("rcs_limit_cache_clear_sdk", rcsUserConfigVo.getUserId().toString(), rcsUserConfigVo.getUserId().toString(), map);
    }

    /**
     * 更新到数据库
     *
     * @param rcsUserSpecialBetLimitConfigsVo
     * @param traderId
     */
    private HttpResponse updateRcsUserSpecialBetLimitConfigData(RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo, String oldStr, Integer traderId, boolean isTrade) {
        //验证参数
        RcsUserConfigVo rcsUserConfigVo = rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo();
        Integer betExtraDelay = rcsUserConfigVo.getBetExtraDelay();
        if (betExtraDelay != null) {
            if (betExtraDelay > 10 || betExtraDelay < 0) {
                return HttpResponse.failToMsg("投注额外延时范围是0-10");
            }
        }
        // 百分比限额数据
        BigDecimal percentageLimit = null;
        List<RcsUserSpecialBetLimitConfigDataVo> rcsUserSpecialBetLimitConfigDataVoList = rcsUserSpecialBetLimitConfigsVo.getRcsUserSpecialBetLimitConfigDataVoList();
        List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigDataVoList)) {
            RcsUserSpecialBetLimitConfigDataVo rcsUserSpecialBetLimitConfigDataVo = rcsUserSpecialBetLimitConfigDataVoList.get(0);
            rcsUserSpecialBetLimitConfigList1 = rcsUserSpecialBetLimitConfigDataVo.getRcsUserSpecialBetLimitConfigList1();
            if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList1)) {
                percentageLimit = rcsUserSpecialBetLimitConfigList1.get(0).getPercentageLimit();
            }
        }
        //处理用户限额配置更新配置
        updateRcsUserConfigVo(rcsUserConfigVo, traderId);
        // 操盘手操作，记录用户限额，投注额外延时，投注额外延时
        if (isTrade) {
            updateToRestrictMerchant(rcsUserConfigVo, percentageLimit, traderId);
        }

        //处理其他配置 先全部变成无效
        updateRcsUserSpecialBetLimitConfig(rcsUserConfigVo, rcsUserSpecialBetLimitConfigList1, traderId);
        //发送mq给业务
        sendMessage(rcsUserConfigVo);

        //把用户的行情标签ID发送给业务
        String uuid = UUID.randomUUID().toString();
        log.info("::{}::oldData2:{}", CommonUtil.getRequestId(), oldStr);
        sendMessage.sendMsg("RCS_USER_TAG_MARKET_LEVEL_ID_TOPIC", "", rcsUserConfigVo.getUserId() + "", oldStr, new HashMap<>(), uuid);
        return HttpResponse.success();
    }

    private void updateRcsUserSpecialBetLimitConfig(RcsUserConfigVo rcsUserConfigVo, List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, Integer traderId) {
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList)) {
            //bug-1592特殊百分比设置单独处理  ##### 并发请求下特殊百分比重复（字段为null导致唯一索引失效）插入表记录 #####
            if (SpecialBettingLimitTypeEnum.PERCENTAGE_LIMIT.getType().equals(rcsUserConfigVo.getSpecialBettingLimit())) {
                log.info("特殊百分比设置单独入库处理：type = {}", rcsUserConfigVo.getSpecialBettingLimit());
                for (RcsUserSpecialBetLimitConfig config : rcsUserSpecialBetLimitConfigList) {
                    //控制首次入库 唯一约束失效问题
                    if (config.getId() == null) {
                        QueryWrapper<RcsUserSpecialBetLimitConfig> queryWrap = new QueryWrapper<>();
                        queryWrap.lambda().eq(RcsUserSpecialBetLimitConfig::getUserId, config.getUserId())
                                .eq(RcsUserSpecialBetLimitConfig::getSpecialBettingLimitType, config.getSpecialBettingLimitType())
                                .eq(RcsUserSpecialBetLimitConfig::getStatus, 1);
                        //兼容可能存在多条的记录 防止报错
                        List<RcsUserSpecialBetLimitConfig> list = rcsUserSpecialBetLimitConfigMapper.selectList(queryWrap);
                        if (!CollectionUtils.isEmpty(list)) {
                            log.info("特殊百分比设置单独入库处理,已经存在：{}条记录,userId：{},specialBettingLimitType：{},limit:{}", list.size(), config.getUserId(), config.getSpecialBettingLimitType(), config.getPercentageLimit());
                            //根据用户id 和限额类型 修改
                            rcsUserSpecialBetLimitConfigMapper.updateUserSpecialBetLimitConfigBy(config);
                        } else {
                            rcsUserSpecialBetLimitConfigMapper.insert(config);
                        }
                        continue;
                    }
                    rcsUserSpecialBetLimitConfigMapper.updateById(config);
                }
                return;
            }
            rcsUserSpecialBetLimitConfigMapper.updateRcsUserSpecialBetLimitConfigStatus(rcsUserConfigVo.getUserId());
            rcsUserSpecialBetLimitConfigMapper.insertOrUpdateUserSpecialBetLimitConfig(rcsUserSpecialBetLimitConfigList);
        }
    }

    /**
     * 更新用户配置
     *
     * @param rcsUserConfigVo
     * @param traderId
     */
    private void updateRcsUserConfigVo(RcsUserConfigVo rcsUserConfigVo, Integer traderId) {

        rcsUserConfigNewService.save(rcsUserConfigVo, traderId);
//        Map<String, Object> columnMap = new HashMap<>();
//        columnMap.put("user_id", rcsUserConfigVo.getUserId());
//        rcsUserConfigMapper.deleteByMap(columnMap);
//        List<Long> sportIdList = rcsUserConfigVo.getSportIdList();
//        List<RcsUserConfig> rcsUserConfigList = new ArrayList<>();
//        for (Long sportId : sportIdList) {
//            RcsUserConfig rcsUserConfig = new RcsUserConfig();
//            rcsUserConfig.setSportId(sportId);
//            rcsUserConfig.setTradeId(traderId.longValue());
//            rcsUserConfig.setBetExtraDelay(rcsUserConfigVo.getBetExtraDelay());
//            rcsUserConfig.setUserId(rcsUserConfigVo.getUserId());
//            rcsUserConfig.setRemarks(rcsUserConfigVo.getRemarks());
//            rcsUserConfig.setSpecialVolume(rcsUserConfigVo.getSpecialVolume());
//            rcsUserConfig.setSettlementInAdvance(rcsUserConfigVo.getSettlementInAdvance());
//            rcsUserConfig.setSpecialBettingLimit(rcsUserConfigVo.getSpecialBettingLimit());
//            rcsUserConfig.setTagMarketLevelId(rcsUserConfigVo.getTagMarketLevelId());
//            rcsUserConfig.setChampionLimitRate(rcsUserConfigVo.getChampionLimitRate());
//            if (rcsUserConfigVo.getSpecialBettingLimit() == null) {
//                rcsUserConfig.setSpecialBettingLimit(0);
//            }
//            rcsUserConfigList.add(rcsUserConfig);
//        }
//        saveBatch(rcsUserConfigList);
    }

    /**
     * 操盘手操作，记录用户限额，投注额外延时，投注额外延时
     *
     * @param rcsUserConfigVo
     */
    private void updateToRestrictMerchant(RcsUserConfigVo rcsUserConfigVo, BigDecimal percentageLimit, Integer traderId) {
        // 删除原有的
        QueryWrapper<RcsTradeRestrictMerchantSetting> delWrapper = new QueryWrapper<RcsTradeRestrictMerchantSetting>();
        delWrapper.lambda().eq(RcsTradeRestrictMerchantSetting::getUserId, rcsUserConfigVo.getUserId());
        rcsTradeRestrictMerchantSettingService.remove(delWrapper);

        List<Long> sportIdList = rcsUserConfigVo.getSportIdList();
        RcsTradeRestrictMerchantSetting rcsTradeRestrictMerchantSetting = new RcsTradeRestrictMerchantSetting();
        rcsTradeRestrictMerchantSetting.setSportIds(CollectionUtil.join(sportIdList, ","));
        rcsTradeRestrictMerchantSetting.setBetExtraDelay(rcsUserConfigVo.getBetExtraDelay());
        rcsTradeRestrictMerchantSetting.setUserId(rcsUserConfigVo.getUserId());
        rcsTradeRestrictMerchantSetting.setPercentageLimit(percentageLimit);
        rcsTradeRestrictMerchantSetting.setBetExtraDelay(rcsUserConfigVo.getBetExtraDelay());
        if(StringUtils.isEmpty(rcsUserConfigVo.getTagMarketLevelId())){
            rcsTradeRestrictMerchantSetting.setTagMarketLevelId(null);
        }else{
            rcsTradeRestrictMerchantSetting.setTagMarketLevelId(Integer.valueOf(rcsUserConfigVo.getTagMarketLevelId()));
        }
        rcsTradeRestrictMerchantSetting.setTradeId(traderId);
        rcsTradeRestrictMerchantSetting.setUpdateTime(new Date());
        rcsTradeRestrictMerchantSettingService.save(rcsTradeRestrictMerchantSetting);
    }

    /**
     * 给业务发生mq
     *
     * @param rcsUserConfigVo
     */
    private void sendMessage(RcsUserConfigVo rcsUserConfigVo) {
        RiskUserLimitVO riskUserLimitVO = new RiskUserLimitVO();
        riskUserLimitVO.setUserId(rcsUserConfigVo.getUserId());
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        riskUserLimitVO.setLastUpdateTime(sdf.format(date));
        sendMessage.sendMessage("rcs_user_quota_config_data", riskUserLimitVO);
    }

    /**
     * @param rcsUserSpecialBetLimitConfigsVo
     * @param traderId
     * @param type                            type=1则走之前存日志的逻辑（日志入库），否则用于构造数据（未包含入库）
     * @param riskMerchantManager
     */
    private void processingLog(RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo, Integer traderId, Integer type, RiskMerchantManager riskMerchantManager) {
        //获取 用户特殊管控 配置数据
        RcsUserConfigVo rcsUserConfigVo = rcsUserSpecialBetLimitConfigsVo.getRcsUserConfigVo();
        //获取 特殊投注限额 配置数据
        List<RcsUserSpecialBetLimitConfigDataVo> rcsUserSpecialBetLimitConfigDataVoList = rcsUserSpecialBetLimitConfigsVo.getRcsUserSpecialBetLimitConfigDataVoList();
        //获取 用户各赛种货量百分比 配置数据
        List<TUserBetRate> userBetRateList = rcsUserSpecialBetLimitConfigsVo.getUserBetRateList();

        //处理 特殊投注限额 数据
        List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigDataVoList)) {
            RcsUserSpecialBetLimitConfigDataVo rcsUserSpecialBetLimitConfigDataVo = rcsUserSpecialBetLimitConfigDataVoList.get(0);
            rcsUserSpecialBetLimitConfigList1 = new ArrayList<>(rcsUserSpecialBetLimitConfigDataVo.getRcsUserSpecialBetLimitConfigList1());
            List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList2 = rcsUserSpecialBetLimitConfigDataVo.getRcsUserSpecialBetLimitConfigList2();
            if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList2)) {
                rcsUserSpecialBetLimitConfigList1.addAll(rcsUserSpecialBetLimitConfigList2);
            }
        }

        //获取数据库中 用户特殊管控 数据
        List<Long> userIdList = new ArrayList<>();
        userIdList.add(rcsUserConfigVo.getUserId());
        HashMap<Long, RcsUserConfigVo> oldRcsUserConfigVoHashMap = getRcsUserConfigVo(userIdList);
        RcsUserConfigVo oldRcsUserConfigVo = oldRcsUserConfigVoHashMap.get(rcsUserConfigVo.getUserId());
        if (oldRcsUserConfigVo == null) {
            oldRcsUserConfigVo = new RcsUserConfigVo();
        }

        ShortSysUserVO traderData = rcsUserSpecialBetLimitConfigsVo.getTraderData();
        if (traderData == null) {
            traderData = rcsTradingAssignmentService.getShortSysUserById(traderId);
        }
        if (traderData == null) {
            traderData = new ShortSysUserVO();
        }

        //获取数据库中 用户各赛种的货量百分比 数据
        LambdaQueryWrapper<TUserBetRate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TUserBetRate::getUserId, rcsUserConfigVo.getUserId());
        List<TUserBetRate> oldUserBetRateList = userBetRateMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(oldUserBetRateList)) {
            oldUserBetRateList = Lists.newArrayList();
        }

        //日志数据组装
        List<RcsOperationLog> rcsOperationLogList = new ArrayList<>();

        List<LogData> logData1 = setTimeLimitLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);
        List<LogData> logData2 = setSportLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);
        List<LogData> logData3 = setSettleLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);
        List<LogData> logData4 = setChampionLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);
        List<LogData> logData5 = setGroupLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);
        List<LogData> logData6 = setBetRateLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);
        List<LogData> logData7 = setBetsLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);
        List<LogData> logData8 = setRemarksLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList);

        if (!CollectionUtils.isEmpty(logData1) && logData1.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData1));
            rcsOperationLog.setShowContent("11");
            rcsOperationLogList.add(rcsOperationLog);
        }
        if (!CollectionUtils.isEmpty(logData2) && logData2.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData2));
            rcsOperationLog.setShowContent("12");
            rcsOperationLogList.add(rcsOperationLog);
        }
        if (!CollectionUtils.isEmpty(logData3) && logData3.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData3));
            rcsOperationLog.setShowContent("13");
            rcsOperationLogList.add(rcsOperationLog);
        }
        if (!CollectionUtils.isEmpty(logData4) && logData4.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData4));
            rcsOperationLog.setShowContent("14");
            rcsOperationLogList.add(rcsOperationLog);
        }
        if (!CollectionUtils.isEmpty(logData5) && logData5.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData5));
            rcsOperationLog.setShowContent("15");
            rcsOperationLogList.add(rcsOperationLog);
        }
        if (!CollectionUtils.isEmpty(logData6) && logData6.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData6));
            rcsOperationLog.setShowContent("16");
            rcsOperationLogList.add(rcsOperationLog);
        }
        if (!CollectionUtils.isEmpty(logData7) && logData7.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData7));
            rcsOperationLog.setShowContent("17");
            rcsOperationLogList.add(rcsOperationLog);
        }

        if (!CollectionUtils.isEmpty(logData8) && logData8.size() > 1) {
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logData8));
            rcsOperationLog.setShowContent("18");
            rcsOperationLogList.add(rcsOperationLog);
        }

        //单独处理
        setQuotaLogData(rcsUserSpecialBetLimitConfigList1, userBetRateList, rcsUserConfigVo, traderData, oldRcsUserConfigVo, oldUserBetRateList, rcsOperationLogList);

        log.info("::{}::日志信息共{}个,详情为{}", CommonUtil.getRequestId(), rcsOperationLogList.size(), JSONObject.toJSONString(rcsOperationLogList));

        //kir-1647-构造商户审核数据
        if (type.equals(1)) {
            //存入日志
            if (!CollectionUtils.isEmpty(rcsOperationLogList)) {
                rcsOperationLogMapper.saveBatchRcsOperationLog(rcsOperationLogList);
            }
        } else {
            //用于拼装
            String recommendValue = "";
            for (RcsOperationLog rcsOperationLog : rcsOperationLogList) {
                if (rcsOperationLog.getShowContent().equals("11")) {
                    //特殊延时
                    riskMerchantManager.setType(3);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            recommendValue = logData.getData() + "s";
                            riskMerchantManager.setRecommendValue(recommendValue);
                            riskMerchantManager.setMerchantShowValue(recommendValue);
                        }
                    }
                } else if (rcsOperationLog.getShowContent().equals("12")) {
                    //特殊延时后面的赛种集合
                    riskMerchantManager.setType(3);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            if (!recommendValue.equals("")) {
                                recommendValue = recommendValue + "-";
                            }
                            recommendValue = recommendValue + logData.getData();
                            riskMerchantManager.setRecommendValue(recommendValue);
                            riskMerchantManager.setMerchantShowValue(recommendValue);
                        }
                    }
                } else if (rcsOperationLog.getShowContent().equals("13")) {
                    //提前结算
                    riskMerchantManager.setType(4);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            riskMerchantManager.setRecommendValue(logData.getData().equals("1") ? "是" : "否");
                            riskMerchantManager.setMerchantShowValue(logData.getData().equals("1") ? "是" : "否");
                        }
                    }
                } else if (rcsOperationLog.getShowContent().equals("15")) {
                    //赔率分组
                    riskMerchantManager.setType(5);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            riskMerchantManager.setRecommendValue(logData.getData());
                            riskMerchantManager.setMerchantShowValue(logData.getData());
                        }
                    }
                } else if (rcsOperationLog.getShowContent().equals("14")) {
                    //冠军玩法限额比例
                    riskMerchantManager.setType(2);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            recommendValue = logData.getName() + ":" + logData.getData() + "%\n";
                            riskMerchantManager.setRecommendValue(recommendValue);
                            riskMerchantManager.setMerchantShowValue(recommendValue);
                        }
                    }
                } else if (rcsOperationLog.getShowContent().equals("17")) {
                    //投注特殊限额
                    riskMerchantManager.setType(2);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            String name = "";
                            if ("1".equals(logData.getData())) {
                                name = "无";
                            } else if ("2".equals(logData.getData())) {
                                name = "特殊百分比限额";
                            } else if ("3".equals(logData.getData())) {
                                name = "特殊单注单场限额";
                            } else if ("4".equals(logData.getData())) {
                                name = "特殊VIP限额";
                            }
                            recommendValue = recommendValue + name + "\n";
                            //bug33452
                            if (!CollectionUtils.isEmpty(logData2) && logData2.size() > 1) {
                                recommendValue = name;
                            }
                            riskMerchantManager.setRecommendValue(recommendValue);
                            riskMerchantManager.setMerchantShowValue(recommendValue);
                        }
                    }
                } else if (rcsOperationLog.getShowContent().equals("20")) {
                    //限额
                    riskMerchantManager.setType(2);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            recommendValue = recommendValue + logData.getName() + "-" + logData.getData() + "%\n";
                            riskMerchantManager.setRecommendValue(recommendValue);
                            riskMerchantManager.setMerchantShowValue(recommendValue);
                        }
                    }
                } else if (rcsOperationLog.getShowContent().equals("21") || rcsOperationLog.getShowContent().equals("22")) {
                    //限额
                    riskMerchantManager.setType(2);
                    List<LogData> list = JSONArray.parseArray(rcsOperationLog.getUpdateContent(), LogData.class);
                    for (LogData logData : list) {
                        if (!logData.getType().equals(UserLogTypeEnum.TRADER.getValue())) {
                            recommendValue = recommendValue + logData.getName() + "-" + logData.getData() + "\n";
                            riskMerchantManager.setRecommendValue(recommendValue);
                            riskMerchantManager.setMerchantShowValue(recommendValue);
                        }
                    }
                }
            }
        }
    }

    //投注额外延时（特殊延时）
    private List<LogData> setTimeLimitLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        //投注额外延时（特殊延时）
        List<LogData> logDataList = new ArrayList<>();
        if (rcsUserConfigVo != null) {
            if (oldRcsUserConfigVo == null) {
                oldRcsUserConfigVo = new RcsUserConfigVo();
            }

            Integer oldBetExtraDelay = oldRcsUserConfigVo.getBetExtraDelay();
            Integer betExtraDelay = rcsUserConfigVo.getBetExtraDelay();
            if (betExtraDelay != null || oldBetExtraDelay != null) {
                boolean i;
                if (betExtraDelay != null) {
                    i = betExtraDelay.equals(oldBetExtraDelay);
                } else {
                    i = oldBetExtraDelay.equals(betExtraDelay);
                }
                if (!i) {
                    LogData logData = new LogData();
                    logData.setName("特殊延时时间");
                    logData.setType(UserLogTypeEnum.TIME_LIMIT.getValue());
                    if (oldBetExtraDelay != null) {
                        logData.setOldData(String.valueOf(oldBetExtraDelay));
                    }
                    if (betExtraDelay != null) {
                        logData.setData(String.valueOf(betExtraDelay));
                    }
                    logDataList.add(logData);
                }
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);

        return logDataList;
    }

    //特殊延时后面的赛种集合
    private List<LogData> setSportLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        List<LogData> logDataList = new ArrayList<>();
        if (rcsUserConfigVo != null) {
            if (oldRcsUserConfigVo == null) {
                oldRcsUserConfigVo = new RcsUserConfigVo();
            }

            //特殊延时后面的赛种集合
            List<Long> oldSportIdList = oldRcsUserConfigVo.getSportIdList();
            List<Long> sportIdList = rcsUserConfigVo.getSportIdList();
            if (!CollectionUtils.isEmpty(oldSportIdList)) {
                Collections.sort(oldSportIdList);
            }
            if (!CollectionUtils.isEmpty(sportIdList)) {
                Collections.sort(sportIdList);
            }
            log.info("::{}::oldSportIdList:{}，sportIdList：{}", CommonUtil.getRequestId(), oldSportIdList, sportIdList);
            if (!CollectionUtils.isEmpty(oldSportIdList) || !CollectionUtils.isEmpty(sportIdList)) {
                boolean i;
                if (!CollectionUtils.isEmpty(oldSportIdList)) {
                    i = oldSportIdList.equals(sportIdList);
                } else {
                    i = sportIdList.equals(oldSportIdList);
                }
                if (!i) {
                    LogData logData = new LogData();
                    logData.setName("特殊延时赛种");
                    //String name = getName(oldSportIdList);
                    logData.setOldData(JSONObject.toJSONString(oldSportIdList));
                    //String name1 = getName(sportIdList);
                    logData.setData(JSONObject.toJSONString(sportIdList));
                    logData.setType(UserLogTypeEnum.SPORT.getValue());
                    logDataList.add(logData);
                }
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);

        return logDataList;
    }

    //提前结算
    private List<LogData> setSettleLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        List<LogData> logDataList = new ArrayList<>();
        if (rcsUserConfigVo != null) {
            if (oldRcsUserConfigVo == null) {
                oldRcsUserConfigVo = new RcsUserConfigVo();
            }
            if (rcsUserConfigVo == null) {
                rcsUserConfigVo = new RcsUserConfigVo();
            }

            //提前结算
            Integer oldsettlementInAdvance = oldRcsUserConfigVo.getSettlementInAdvance();
            Integer settlementInAdvance = rcsUserConfigVo.getSettlementInAdvance();
            if (oldsettlementInAdvance != null || settlementInAdvance != null) {
                boolean i;
                if (settlementInAdvance != null) {
                    i = settlementInAdvance.equals(oldsettlementInAdvance);
                } else {
                    i = oldsettlementInAdvance.equals(settlementInAdvance);
                }
                if (!i) {
                    LogData logData = new LogData();
                    logData.setType(UserLogTypeEnum.SETTLE.getValue());
                    logData.setName("提前结算");
                    if (oldsettlementInAdvance != null) {
                        logData.setOldData(String.valueOf(oldsettlementInAdvance));
                    }
                    if (settlementInAdvance != null) {
                        logData.setData(String.valueOf(settlementInAdvance));
                    }
                    logDataList.add(logData);
                }
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);

        return logDataList;
    }

    //冠军玩法限额比例
    private List<LogData> setChampionLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        List<LogData> logDataList = new ArrayList<>();
        if (rcsUserConfigVo != null) {
            //冠军玩法限额比例
            BigDecimal oldchampionLimitRate = oldRcsUserConfigVo.getChampionLimitRate() == null ? null : oldRcsUserConfigVo.getChampionLimitRate().setScale(2);
            BigDecimal championLimitRate = rcsUserConfigVo.getChampionLimitRate() == null ? null : rcsUserConfigVo.getChampionLimitRate().setScale(2);
            if (oldchampionLimitRate != null || championLimitRate != null) {
                boolean i;
                if (championLimitRate != null) {
                    i = championLimitRate.equals(oldchampionLimitRate);
                } else {
                    i = oldchampionLimitRate.equals(championLimitRate);
                }
                if (!i) {
                    LogData logData = new LogData();
                    logData.setType(UserLogTypeEnum.CHAMPION.getValue());
                    logData.setName("冠军玩法限额比例");
                    if (oldchampionLimitRate != null) {
                        logData.setOldData(oldchampionLimitRate + "");
                    }
                    if (championLimitRate != null) {
                        logData.setData(championLimitRate + "");
                    }
                    logDataList.add(logData);
                }
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);

        return logDataList;
    }

    //赔率分组
    private List<LogData> setGroupLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        List<LogData> logDataList = new ArrayList<>();
        if (rcsUserConfigVo != null) {
            if (oldRcsUserConfigVo == null) {
                oldRcsUserConfigVo = new RcsUserConfigVo();
            }
            if (rcsUserConfigVo == null) {
                rcsUserConfigVo = new RcsUserConfigVo();
            }

            //赔率分组
            String oldTagMarketLevelId = oldRcsUserConfigVo.getTagMarketLevelId();
            String tagMarketLevelId = rcsUserConfigVo.getTagMarketLevelId();
            log.info("::{}:: 新赔率分组{}，旧赔率分组{}",rcsUserConfigVo.getUserId(),tagMarketLevelId,oldTagMarketLevelId);
            if (null != tagMarketLevelId || null != oldTagMarketLevelId) {
                boolean i;
                if (tagMarketLevelId != null) {
                    i = tagMarketLevelId.equals(oldTagMarketLevelId);
                } else {
                    i = oldTagMarketLevelId.equals(tagMarketLevelId);
                }
                if (!i) {
                    LogData logData = new LogData();
                    logData.setType(UserLogTypeEnum.GROUP.getValue());
                    logData.setName("赔率分组");
                    if (tagMarketLevelId != null) {
                        logData.setOldData(oldTagMarketLevelId);
                    }
                    if (tagMarketLevelId != null) {
                        logData.setData(tagMarketLevelId);
                    }
                    //bug-30236 如果新用户默认为空则不存
                    //bug-30626 库中若为null，则默认为新用户，新用户第一次保存若传过来的数据为 0 ，则不处理
                    if(ObjectUtils.isNotEmpty(logData.getData()) || ObjectUtils.isNotEmpty(logData.getOldData())) {
                        logDataList.add(logData);
                    }
                }
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);

        return logDataList;
    }

    //用户各赛种货量百分比
    private List<LogData> setBetRateLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        List<LogData> logDataList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userBetRateList)) {
            //用户各赛种货量百分比
            //boolean i = org.apache.commons.collections4.CollectionUtils.isEqualCollection(userBetRateList, oldUserBetRateList);
            Map<String, String> oldCollect = oldUserBetRateList.stream().collect(Collectors.toMap(e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2)), e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2))));
            Map<String, String> collect = userBetRateList.stream().collect(Collectors.toMap(e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2)), e -> String.valueOf(e.getSportId()) + String.valueOf(e.getBetRate().setScale(2))));

            boolean i = true;
            if (oldCollect.size() != collect.size()) {
                i = false;
            } else {
                if (!oldCollect.equals(collect)) {
                    i = false;
                }
            }

            if (!i) {
                LogData logData = new LogData();
                logData.setType(UserLogTypeEnum.BETRATE.getValue());
                logData.setName("用户各赛种货量百分比");
                logData.setOldData(JSONObject.toJSONString(oldUserBetRateList));
                logData.setData(JSONObject.toJSONString(userBetRateList));
                logDataList.add(logData);
            }
        } else {
            if (!CollectionUtils.isEmpty(oldUserBetRateList)) {
                LogData logData = new LogData();
                logData.setType(UserLogTypeEnum.BETRATE.getValue());
                logData.setName("用户各赛种货量百分比");
                logData.setOldData(JSONObject.toJSONString(oldUserBetRateList));
                logData.setData(JSONObject.toJSONString(userBetRateList));
                logDataList.add(logData);
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);

        return logDataList;
    }

    //投注特殊限额
    private List<LogData> setBetsLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        List<LogData> logDataList = new ArrayList<>();
        //投注特殊限额
        if (rcsUserConfigVo.getSpecialBettingLimit() != null || oldRcsUserConfigVo.getSpecialBettingLimit() != null) {
            boolean i;
            if (rcsUserConfigVo.getSpecialBettingLimit() != null) {
                i = rcsUserConfigVo.getSpecialBettingLimit().equals(oldRcsUserConfigVo.getSpecialBettingLimit());
            } else {
                i = oldRcsUserConfigVo.getSpecialBettingLimit().equals(rcsUserConfigVo.getSpecialBettingLimit());
            }
            if (!i) {
                LogData logData4 = new LogData();
                logData4.setName("特殊限额");
                logData4.setType(UserLogTypeEnum.BETS.getValue());
                if (oldRcsUserConfigVo.getSpecialBettingLimit() != null) {
                    //logData4.setOldData(SpecialBettingLimitTypeEnum.getName(oldRcsUserConfigVo.getSpecialBettingLimit()));
                    logData4.setOldData(String.valueOf(oldRcsUserConfigVo.getSpecialBettingLimit()));
                }
                //logData4.setData(SpecialBettingLimitTypeEnum.getName(rcsUserConfigVo.getSpecialBettingLimit()));
                logData4.setData(String.valueOf(rcsUserConfigVo.getSpecialBettingLimit()));
                logDataList.add(logData4);
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);
        return logDataList;
    }

    //限额
    private void setQuotaLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList, List<RcsOperationLog> rcsOperationLogList) {

        if (!CollectionUtils.isEmpty(rcsUserSpecialBetLimitConfigList)) {

            //日志过滤生效的数据
            rcsUserSpecialBetLimitConfigList = rcsUserSpecialBetLimitConfigList.stream().filter(e -> e.getStatus() == 1).collect(Collectors.toList());

            //类型是否一样
            Boolean b = equels(oldRcsUserConfigVo.getSpecialBettingLimit(), rcsUserConfigVo.getSpecialBettingLimit());
            for (RcsUserSpecialBetLimitConfig rcsUserSpecialBetLimitConfig : rcsUserSpecialBetLimitConfigList) {
                //限额
                List<LogData> logDataList = new ArrayList<>();
                Integer orderType = rcsUserSpecialBetLimitConfig.getOrderType();
                String firstName = null;
                String secondName = null;

                //国际化 类型前缀
                String fstStr = null;
                String midStr = null;
                String endStr = null;

                if (orderType != null) {
                    if (orderType == 1) {
                        firstName = "单关";
                        fstStr = "1";
                    } else {
                        firstName = "串关";
                        fstStr = "2";
                    }
                    Integer sportId = rcsUserSpecialBetLimitConfig.getSportId();
                    if (sportId == 1) {
                        secondName = "足球";
                        endStr = "1";
                    } else if (sportId == 2) {
                        secondName = "篮球";
                        endStr = "2";
                    } else if (sportId == 0) {
                        secondName = "其他";
                        endStr = "3";
                    } else {
                        secondName = "全部";
                        endStr = "0";
                    }
                }
                if (rcsUserConfigVo.getSpecialBettingLimit().equals(SpecialBettingLimitTypeEnum.PERCENTAGE_LIMIT.getType())) {
                    if (!b) {
                        LogData logData = new LogData();
                        logData.setName("限额百分比");
                        logData.setData(rcsUserSpecialBetLimitConfig.getPercentageLimit().setScale(4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2) + "%");
                        logData.setType(UserLogTypeEnum.QUOTA.getValue());
                        logDataList.add(logData);
                        //操作人
                        LogData logData9 = new LogData();
                        logData9.setType(UserLogTypeEnum.TRADER.getValue());
                        logData9.setName("操作人");
                        logData9.setData(traderData.getUserCode());
                        logDataList.add(logData9);

                        if (!CollectionUtils.isEmpty(logDataList) && logDataList.size() > 1) {
                            RcsOperationLog rcsOperationLog = new RcsOperationLog();
                            rcsOperationLog.setHandleCode("user_config_history");
                            rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
                            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));

                            rcsOperationLog.setShowContent("20");
                            rcsOperationLogList.add(rcsOperationLog);
                        }

                    } else {
                        if (!equels(rcsUserSpecialBetLimitConfig.getPercentageLimit(), rcsUserSpecialBetLimitConfig.getOldPercentageLimit())) {
                            LogData logData = new LogData();
                            logData.setName("限额百分比");
                            if (rcsUserSpecialBetLimitConfig.getOldPercentageLimit() != null) {
                                logData.setOldData(rcsUserSpecialBetLimitConfig.getOldPercentageLimit().setScale(4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2) + "%");
                            }
                            if (rcsUserSpecialBetLimitConfig.getPercentageLimit() != null) {
                                logData.setData(rcsUserSpecialBetLimitConfig.getPercentageLimit().setScale(4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2) + "%");
                            }
                            logData.setType(UserLogTypeEnum.QUOTA.getValue());
                            logDataList.add(logData);
                            //操作人
                            LogData logData9 = new LogData();
                            logData9.setType(UserLogTypeEnum.TRADER.getValue());
                            logData9.setName("操作人");
                            logData9.setData(traderData.getUserCode());
                            logDataList.add(logData9);
                            if (!CollectionUtils.isEmpty(logDataList) && logDataList.size() > 1) {
                                RcsOperationLog rcsOperationLog = new RcsOperationLog();
                                rcsOperationLog.setHandleCode("user_config_history");
                                rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
                                rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));

                                rcsOperationLog.setShowContent("20");
                                rcsOperationLogList.add(rcsOperationLog);
                            }
                        }
                    }
                } else {
                    if (!b) {
                        LogData logData = new LogData();
                        StringBuilder stringBuilder = new StringBuilder();
                        if (rcsUserSpecialBetLimitConfig.getOrderType() == 2) {
                            stringBuilder.append(firstName).append("单日赔付限额-").append(secondName);
                            midStr = "1";
                        } else {
                            stringBuilder.append(firstName).append("单场赔付限额-").append(secondName);
                            midStr = "2";
                        }
                        logData.setName(stringBuilder.toString());
                        if (rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit() != null) {
                            logData.setData(String.valueOf(rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit()));

                            //logData.setType(UserLogTypeEnum.QUOTA.getValue());
                            StringBuilder typeStr = new StringBuilder();
                            typeStr.append(fstStr).append(midStr).append(endStr);
                            logData.setType(Integer.valueOf(String.valueOf(typeStr)));

                            logDataList.add(logData);
                            //操作人
                            LogData logData9 = new LogData();
                            logData9.setType(UserLogTypeEnum.TRADER.getValue());
                            logData9.setName("操作人");
                            logData9.setData(traderData.getUserCode());
                            logDataList.add(logData9);
                            if (!CollectionUtils.isEmpty(logDataList) && logDataList.size() > 1) {
                                RcsOperationLog rcsOperationLog = new RcsOperationLog();
                                rcsOperationLog.setHandleCode("user_config_history");
                                rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
                                rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));

                                rcsOperationLog.setShowContent("21");
                                rcsOperationLogList.add(rcsOperationLog);
                            }
                        }
                        LogData logData1 = new LogData();
                        StringBuilder stringBuilder1 = new StringBuilder();
                        stringBuilder1.append(firstName).append("单注赔付限额-").append(secondName);
                        logData1.setName(stringBuilder1.toString());
                        if (rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit() != null) {
                            midStr = "3";
                            logDataList = new ArrayList<>();
                            logData1.setData(String.valueOf(rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit()));

                            //logData1.setType(UserLogTypeEnum.QUOTA.getValue());
                            StringBuilder typeStr = new StringBuilder();
                            typeStr.append(fstStr).append(midStr).append(endStr);
                            logData1.setType(Integer.valueOf(String.valueOf(typeStr)));

                            logDataList.add(logData1);
                            //操作人
                            LogData logData9 = new LogData();
                            logData9.setType(UserLogTypeEnum.TRADER.getValue());
                            logData9.setName("操作人");
                            logData9.setData(traderData.getUserCode());
                            logDataList.add(logData9);
                            if (!CollectionUtils.isEmpty(logDataList) && logDataList.size() > 1) {
                                RcsOperationLog rcsOperationLog = new RcsOperationLog();
                                rcsOperationLog.setHandleCode("user_config_history");
                                rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
                                rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));

                                rcsOperationLog.setShowContent("22");
                                rcsOperationLogList.add(rcsOperationLog);
                            }
                        }
                    } else {
                        if (!equels(rcsUserSpecialBetLimitConfig.getOldSingleGameClaimLimit(), rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit())) {
                            LogData logData = new LogData();
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(firstName).append("单日赔付限额-").append(secondName);
                            logData.setName(stringBuilder.toString());
                            if (rcsUserSpecialBetLimitConfig.getOldSingleGameClaimLimit() != null) {
                                logData.setOldData(String.valueOf(rcsUserSpecialBetLimitConfig.getOldSingleGameClaimLimit()));
                            }
                            if (rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit() != null) {
                                logData.setData(String.valueOf(rcsUserSpecialBetLimitConfig.getSingleGameClaimLimit()));
                            }

                            midStr = "2";
                            //logData.setType(UserLogTypeEnum.QUOTA.getValue());
                            StringBuilder typeStr = new StringBuilder();
                            typeStr.append(fstStr).append(midStr).append(endStr);
                            logData.setType(Integer.valueOf(String.valueOf(typeStr)));

                            logDataList.add(logData);
                            //操作人
                            LogData logData9 = new LogData();
                            logData9.setType(UserLogTypeEnum.TRADER.getValue());
                            logData9.setName("操作人");
                            logData9.setData(traderData.getUserCode());
                            logDataList.add(logData9);
                            if (!CollectionUtils.isEmpty(logDataList) && logDataList.size() > 1) {
                                RcsOperationLog rcsOperationLog = new RcsOperationLog();
                                rcsOperationLog.setHandleCode("user_config_history");
                                rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
                                rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));

                                rcsOperationLog.setShowContent("21");
                                rcsOperationLogList.add(rcsOperationLog);
                            }
                        }
                        if (!equels(rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit(), rcsUserSpecialBetLimitConfig.getOldSingleNoteClaimLimit())) {
                            logDataList = new ArrayList<>();
                            LogData logData = new LogData();
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(firstName).append("单注赔付限额-").append(secondName);
                            logData.setName(stringBuilder.toString());
                            if (rcsUserSpecialBetLimitConfig.getOldSingleNoteClaimLimit() != null) {
                                logData.setOldData(String.valueOf(rcsUserSpecialBetLimitConfig.getOldSingleNoteClaimLimit()));
                            }
                            if (rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit() != null) {
                                logData.setData(String.valueOf(rcsUserSpecialBetLimitConfig.getSingleNoteClaimLimit()));
                            }
                            logData.setType(UserLogTypeEnum.QUOTA.getValue());

                            midStr = "3";
                            //logData.setType(UserLogTypeEnum.QUOTA.getValue());
                            StringBuilder typeStr = new StringBuilder();
                            typeStr.append(fstStr).append(midStr).append(endStr);
                            logData.setType(Integer.valueOf(String.valueOf(typeStr)));

                            logDataList.add(logData);
                            //操作人
                            LogData logData9 = new LogData();
                            logData9.setType(UserLogTypeEnum.TRADER.getValue());
                            logData9.setName("操作人");
                            logData9.setData(traderData.getUserCode());
                            logDataList.add(logData9);
                            if (!CollectionUtils.isEmpty(logDataList) && logDataList.size() > 1) {
                                RcsOperationLog rcsOperationLog = new RcsOperationLog();
                                rcsOperationLog.setHandleCode("user_config_history");
                                rcsOperationLog.setHanlerId(String.valueOf(rcsUserConfigVo.getUserId()));
                                rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));

                                rcsOperationLog.setShowContent("22");
                                rcsOperationLogList.add(rcsOperationLog);
                            }
                        }
                    }
                }
            }
        }
    }

    //备注
    private List<LogData> setRemarksLogData(List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList, List<TUserBetRate> userBetRateList, RcsUserConfigVo rcsUserConfigVo, ShortSysUserVO traderData, RcsUserConfigVo oldRcsUserConfigVo, List<TUserBetRate> oldUserBetRateList) {
        List<LogData> logDataList = new ArrayList<>();
        //备注
        if (rcsUserConfigVo.getRemarks() != null || oldRcsUserConfigVo.getRemarks() != null) {
            boolean i;
            if (rcsUserConfigVo.getRemarks() != null) {
                i = rcsUserConfigVo.getRemarks().equals(oldRcsUserConfigVo.getRemarks());
            } else {
                i = oldRcsUserConfigVo.getRemarks().equals(rcsUserConfigVo.getRemarks());
            }
            if (!i) {
                LogData logData4 = new LogData();
                logData4.setName("备注");
                logData4.setType(UserLogTypeEnum.REMARKS.getValue());
                if (oldRcsUserConfigVo.getSpecialBettingLimit() != null) {
                    logData4.setOldData(oldRcsUserConfigVo.getRemarks());
                }
                logData4.setData(rcsUserConfigVo.getRemarks());
                logDataList.add(logData4);
            }
        }

        //操作人
        LogData logData1 = new LogData();
        logData1.setType(UserLogTypeEnum.TRADER.getValue());
        logData1.setName("操作人");
        logData1.setData(traderData.getUserCode());
        logDataList.add(logData1);

        return logDataList;
    }

    public boolean equels(BigDecimal oldBigDecimal, BigDecimal bigDecimal) {
        if (oldBigDecimal == null) {
            if (bigDecimal == null) {
                return true;
            }
        } else {
            return oldBigDecimal.equals(bigDecimal);
        }
        return false;
    }

    public boolean equels(Long oldBigDecimal, Long bigDecimal) {
        if (oldBigDecimal == null) {
            if (bigDecimal == null) {
                return true;
            }
        } else {
            return oldBigDecimal.equals(bigDecimal);
        }
        return false;
    }

    public boolean equels(Integer oldBigDecimal, Integer bigDecimal) {
        if (oldBigDecimal == null) {
            if (bigDecimal == null) {
                return true;
            }
        } else {
            return oldBigDecimal.equals(bigDecimal);
        }
        return false;
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
}
