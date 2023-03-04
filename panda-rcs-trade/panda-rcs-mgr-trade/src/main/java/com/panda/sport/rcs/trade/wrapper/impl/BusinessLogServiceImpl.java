package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.BusinessLogVo;
import com.panda.sport.rcs.trade.vo.SportIdVo;
import com.panda.sport.rcs.vo.RcsLabelLimitConfigVo;
import com.panda.sport.rcs.vo.RcsLabelSportVolumePercentageVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * <p>
 * 业务风控处理日志
 * </p>
 *
 * @author skyKong
 * @since 2023-02-06
 */
@Slf4j
public class BusinessLogServiceImpl  implements Callable<List<RcsQuotaBusinessLimitLog>> {

    private BusinessLogVo businessLogVo;
    private final String logTitle="风控措施管理";

    private final String log_Insert="新增";

    private final String LOG_IS_NO="无";
    private final String LOG_IS_YES="有";

    private final String SPORT_ALL="全部赛种";

    private final String SPORT_PERCENTAGE="%";

  public BusinessLogServiceImpl(BusinessLogVo businessLogVo){
    this.businessLogVo=businessLogVo;
  }
    /**
     * 风控措施异步处理
     * */
    @Override
    public List<RcsQuotaBusinessLimitLog> call() throws Exception {

        List<RcsQuotaBusinessLimitLog> list=new ArrayList<>();
        final RcsLabelLimitConfig[] limitLog = {new RcsLabelLimitConfig()};
        try {
            for (RcsLabelLimitConfigVo item : businessLogVo.getNewsLabelLimitConfigs()) {
                List<Integer> oldSportIdList=new ArrayList<>();
                List<RcsLabelLimitConfig> rcsLabelLimitConfigs = businessLogVo.getOldLabelLimitConfigs().stream().filter(t -> t.getTagId().equals(item.getTagId())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(rcsLabelLimitConfigs)){
                    rcsLabelLimitConfigs.forEach(it->{
                        if(Objects.nonNull(it.getSportId())){
                            oldSportIdList.add(it.getSportId());
                        }
                    });
                    RcsLabelLimitConfig oldLimitConfig=rcsLabelLimitConfigs.get(0);
                    RcsLabelLimitConfig newLimitConfig = new RcsLabelLimitConfig();
                    BeanCopyUtils.copyProperties(item, newLimitConfig);
                    limitLog[0]=newLimitConfig;
                    newLimitConfig.setLimitPercentage(item.getLimitPercentage());
                    newLimitConfig.setVolumePercentage(item.getVolumePercentage().divide(new BigDecimal(100)));
                    List<Integer> newSportIdList=item.getSportIdList();
                    if(newSportIdList == null){
                        newSportIdList=new ArrayList<>();
                    }
                    setBusinessLimitLog(newLimitConfig, oldLimitConfig,oldSportIdList,newSportIdList).forEach(it -> {
                        RcsQuotaBusinessLimitLog businessLimitLog = list.stream().filter(e -> e.getObjectId().equals(it.getObjectId()) && e.getParamName().equals(it.getParamName())).findFirst().orElse(null);
                        if (Objects.isNull(businessLimitLog)) {
                            list.add(it);
                        }
                    });
                }else {
                    RcsLabelLimitConfig newLimitConfig = new RcsLabelLimitConfig();
                    BeanCopyUtils.copyProperties(item, newLimitConfig);
                    setBusinessLimitLog(newLimitConfig,item.getSportIdList()).forEach(it -> {
                        RcsQuotaBusinessLimitLog businessLimitLog = list.stream().filter(e -> e.getObjectId().equals(it.getObjectId()) && e.getParamName().equals(it.getParamName())).findFirst().orElse(null);
                        if (Objects.isNull(businessLimitLog)) {
                            list.add(it);
                        }
                    });
                }
                List<RcsLabelSportVolumePercentageVo> volumePercentageVoList = item.getSportVolumePercentageList();
                if (volumePercentageVoList == null) {
                    volumePercentageVoList = new ArrayList<>();
                }
                List<RcsLabelSportVolumePercentage> oldLabelSportVolumePercentages =businessLogVo.getOldLabelSportVolumePercentages().stream().filter(t->t.getTagId().equals(item.getTagId())).collect(Collectors.toList());
                List<RcsLabelSportVolumePercentageVo> finalVolumePercentageVoList = volumePercentageVoList;
                volumePercentageVoList.forEach(volumeItem->{
                    RcsLabelSportVolumePercentage oldLabelSportVolumePercentage = businessLogVo.getOldLabelSportVolumePercentages().stream().filter(t->t.getTagId().equals(volumeItem.getTagId()) && t.getSportId().equals(volumeItem.getSportId())).findFirst().orElse(null);
                    if(Objects.nonNull(oldLabelSportVolumePercentage)){
                        List<RcsQuotaBusinessLimitLog> newsList = setBusinessLimitLog(oldLabelSportVolumePercentage,volumeItem);
                        newsList.forEach(it->{
                            list.add(it);
                        });
                    }else{
                        RcsQuotaBusinessLimitLog businessLimitLogVo = setNewBusinessLimitLog(volumeItem);
                        if(Objects.nonNull(businessLimitLogVo)){
                            list.add(businessLimitLogVo);
                        }
                    }
                });
                //删除便签货
                oldLabelSportVolumePercentages.forEach(labelVolume->{
                    RcsLabelSportVolumePercentageVo labelSportVolumePercentageVo =  finalVolumePercentageVoList.stream().filter(t->t.getTagId().equals(labelVolume.getTagId()) && t.getSportId().equals(labelVolume.getSportId())).findFirst().orElse(null);
                    if(Objects.isNull(labelSportVolumePercentageVo)){
                        list.add(delLabelVolumePercentageLog(labelVolume));
                    }
                });
            }
        }catch (Exception e){
            log.info("操作失败{}", JSONObject.toJSONString(limitLog[0]));
            throw  new RuntimeException("操作失败"+e);
        }
        return list;
    }
    /**
     * 修改状态修改设置日志
     * */
    private List<RcsQuotaBusinessLimitLog> setBusinessLimitLog(RcsLabelSportVolumePercentage old,RcsLabelSportVolumePercentageVo news){
        List<RcsQuotaBusinessLimitLog> list=new ArrayList<>();
        //只修改赛种
        if (!news.getSportId().equals(old.getSportId())){
            String beforeVal="";
            String afterVal="";
            SportIdVo newSportIdVo=CommonUtil.setBusiness().stream().filter(s->s.getSportId() ==news.getSportId().intValue()).findFirst().orElse(null);
            if(Objects.nonNull(newSportIdVo)){
                afterVal=newSportIdVo.getSportName();
            }
            SportIdVo oldSportIdVo=CommonUtil.setBusiness().stream().filter(s->s.getSportId() ==old.getSportId().intValue()).findFirst().orElse(null);
            if(Objects.nonNull(oldSportIdVo)){
                beforeVal=oldSportIdVo.getSportName();
            }
            if(0 == news.getSportId()){
                afterVal=SPORT_ALL;
            }
            if(0 == old.getSportId()){
                beforeVal=SPORT_ALL;
            }
            if(!afterVal.equals(beforeVal)) {
                String oldVolume= old.getVolumePercentage() == null ? "": old.getVolumePercentage().intValue()+SPORT_PERCENTAGE;
                String newVolume= news.getVolumePercentage() == null ? "": news.getVolumePercentage().intValue()+SPORT_PERCENTAGE;
                if(StringUtils.isNotEmpty(oldVolume)){
                    beforeVal = beforeVal+"-"+oldVolume;
                }
                if(StringUtils.isNotEmpty(newVolume)){
                    afterVal  = afterVal +"-"+newVolume;
                }
                list.add(setBusinessLimitLog(news, CommonUtil.Volume_Percentage, beforeVal, afterVal));
            }
        }
        //只修改货量
        if (news.getVolumePercentage().compareTo(old.getVolumePercentage()) !=0){
            String beforeVal="";
            String afterVal="";
            SportIdVo newSportIdVo=CommonUtil.setBusiness().stream().filter(s->s.getSportId() ==news.getSportId().intValue()).findFirst().orElse(null);
            if(Objects.nonNull(newSportIdVo)){
                afterVal=newSportIdVo.getSportName();
            }
            SportIdVo oldSportIdVo=CommonUtil.setBusiness().stream().filter(s->s.getSportId() ==old.getSportId().intValue()).findFirst().orElse(null);
            if(Objects.nonNull(oldSportIdVo)){
                beforeVal=oldSportIdVo.getSportName();
            }
            if(0 == news.getSportId()){
                afterVal=SPORT_ALL;
            }
            if(0 == old.getSportId()){
                beforeVal=SPORT_ALL;
            }
            String oldVolume= old.getVolumePercentage() == null ? "": old.getVolumePercentage().intValue()+SPORT_PERCENTAGE;
            String newVolume= news.getVolumePercentage() == null ? "": news.getVolumePercentage().intValue()+SPORT_PERCENTAGE;
            if(StringUtils.isNotEmpty(oldVolume)){
                beforeVal = beforeVal+"-"+oldVolume;
            }
            if(StringUtils.isNotEmpty(newVolume)){
                afterVal  = afterVal +"-"+newVolume;
            }
            list.add(setBusinessLimitLog(news, CommonUtil.Volume_Percentage, beforeVal, afterVal));
        }
        return list;
    }
    /**
     * 初始化状态设置日志
     * */
    private RcsQuotaBusinessLimitLog setNewBusinessLimitLog(RcsLabelSportVolumePercentageVo news){
        RcsQuotaBusinessLimitLog rcsQuotaBusinessLimitLog=new RcsQuotaBusinessLimitLog();
        if(Objects.nonNull(news.getSportId())) {
            String afterVal="";
            SportIdVo newSportIdVo=CommonUtil.setBusiness().stream().filter(s->s.getSportId() == news.getSportId().intValue()).findFirst().orElse(null);
            if(Objects.nonNull(newSportIdVo)){
                afterVal=newSportIdVo.getSportName();
            }
            if(0 == news.getSportId()){
                afterVal=SPORT_ALL;
            }
            String newVolume= news.getVolumePercentage() == null ? "": news.getVolumePercentage().intValue()+SPORT_PERCENTAGE;
            if(StringUtils.isNotEmpty(newVolume)){
                afterVal = afterVal+"-"+newVolume;
            }
            rcsQuotaBusinessLimitLog = setBusinessLimitLog(news, CommonUtil.Volume_Percentage, "", afterVal);
        }
       return rcsQuotaBusinessLimitLog;
    }
    /**
     * 删除措施标签
     * */
    private RcsQuotaBusinessLimitLog delLabelVolumePercentageLog(RcsLabelSportVolumePercentage old){
        String beforeVal="";
        if(0 == old.getSportId()){
            beforeVal=SPORT_ALL;
        }else{
            SportIdVo sportIdVo=CommonUtil.setBusiness().stream().filter(t->t.getSportId() == old.getSportId().intValue()).findFirst().orElse(null);
            if(Objects.nonNull(sportIdVo)){
                beforeVal =sportIdVo.getSportName();
            }
        }
        String oldVolume= old.getVolumePercentage() == null ? "": old.getVolumePercentage().intValue()+SPORT_PERCENTAGE;
        if(StringUtils.isNotEmpty(oldVolume)){
            beforeVal = beforeVal+"-"+oldVolume;
        }
        RcsQuotaBusinessLimitLog quotaBusinessLimitLog = setBusinessLimitLog(old,CommonUtil.Volume_Percentage,beforeVal,"");
        return  quotaBusinessLimitLog;
    }
    /**
     * 标签配置日志
     * */
    private RcsQuotaBusinessLimitLog setBusinessLimitLog(RcsLabelLimitConfig rcsLabelLimitConfig,String paramName,
                                                         String beforeVal,String afterVal){
        RcsQuotaBusinessLimitLog limitLoglog = new RcsQuotaBusinessLimitLog();
        TUserLevel userLeve = businessLogVo.getTUserLevels().stream().filter(level -> level.getLevelId().equals(rcsLabelLimitConfig.getTagId())).findFirst().orElse(null);
        limitLoglog.setOperateCategory(logTitle);
        limitLoglog.setObjectId(rcsLabelLimitConfig.getTagId().toString());
        limitLoglog.setObjectName(userLeve.getLevelName());
        limitLoglog.setExtObjectId("-");
        limitLoglog.setExtObjectName("-");
        limitLoglog.setOperateType(CommonUtil.logCode);
        limitLoglog.setParamName(paramName);
        limitLoglog.setBeforeVal(beforeVal);
        limitLoglog.setAfterVal(afterVal);
        limitLoglog.setUserId(businessLogVo.getUserId());
        return limitLoglog;
    }
    /**
     * 货量标签配置日志
     * */
    private RcsQuotaBusinessLimitLog setBusinessLimitLog(RcsLabelSportVolumePercentageVo sportVolumePercentage,String paramName,
                                                         String beforeVal,String afterVal){
        RcsQuotaBusinessLimitLog limitLoglog = new RcsQuotaBusinessLimitLog();
        try{
            TUserLevel userLeve = businessLogVo.getTUserLevels().stream().filter(level -> level.getLevelId().equals(sportVolumePercentage.getTagId())).findFirst().orElse(null);
            limitLoglog.setOperateCategory(logTitle);
            limitLoglog.setObjectId(sportVolumePercentage.getTagId().toString());
            limitLoglog.setObjectName(userLeve.getLevelName());
            limitLoglog.setExtObjectId("-");
            limitLoglog.setExtObjectName("-");
            limitLoglog.setOperateType(CommonUtil.logCode);
            limitLoglog.setParamName(paramName);
            limitLoglog.setBeforeVal(beforeVal);
            limitLoglog.setAfterVal(afterVal);
            limitLoglog.setUserId(businessLogVo.getUserId());
        }catch (Exception ex) {
            log.error("操作异常{}",ex.getMessage(),ex);
             throw  new RuntimeException("操作异常");
        }
        return limitLoglog;
    }
    /**
     * 货量标签处理
     * */
    private RcsQuotaBusinessLimitLog setBusinessLimitLog(RcsLabelSportVolumePercentage sportVolumePercentage,String paramName,
                                                         String beforeVal,String afterVal){
        RcsQuotaBusinessLimitLog limitLoglog = new RcsQuotaBusinessLimitLog();
        try{
            TUserLevel userLeve = businessLogVo.getTUserLevels().stream().filter(level -> level.getLevelId().equals(sportVolumePercentage.getTagId())).findFirst().orElse(null);
            limitLoglog.setOperateCategory(logTitle);
            limitLoglog.setObjectId(sportVolumePercentage.getTagId().toString());
            limitLoglog.setObjectName(userLeve.getLevelName());
            limitLoglog.setExtObjectId("-");
            limitLoglog.setExtObjectName("-");
            limitLoglog.setOperateType(CommonUtil.logCode);
            limitLoglog.setParamName(paramName);
            limitLoglog.setBeforeVal(beforeVal);
            limitLoglog.setAfterVal(afterVal);
            limitLoglog.setUserId(businessLogVo.getUserId());
        }catch (Exception ex) {
            log.error("操作异常{}",ex.getMessage(),ex);
            throw  new RuntimeException("操作异常");
        }
        return limitLoglog;
    }
    /**
     * 标签设置日志
     * */
    private List<RcsQuotaBusinessLimitLog> setBusinessLimitLog(RcsLabelLimitConfig item,RcsLabelLimitConfig rcsLabelLimitConfig,List<Integer> oldSports,List<Integer> newSports){
        List<RcsQuotaBusinessLimitLog> list=new ArrayList<>();
        List<SportIdVo> sportIdVoList =CommonUtil.setBusiness();

        String oString =  JSONArray.toJSONString(oldSports);
        String nString =  JSONArray.toJSONString(newSports);
        if(!nString.equals(oString)){
            String beforeVal = "";
            if(!Objects.isNull(oldSports)){
                StringBuilder sb=new StringBuilder();
                oldSports.forEach(t->{
                    SportIdVo sportIdVo=sportIdVoList.stream().filter(s->s.getSportId() == t.intValue()).findFirst().orElse(null);
                    if(Objects.nonNull(sportIdVo)){
                        sb.append(sportIdVo.getSportName()+",");
                    }
                });
                if(sb.length() >0){
                    beforeVal = sb.substring(0,sb.length()-1);
                }
            }
            String afterVal =  "";
            if(!Objects.isNull(newSports)){
                StringBuilder sb=new StringBuilder();
                newSports.forEach(t->{
                    SportIdVo sportIdVo=sportIdVoList.stream().filter(s->s.getSportId() == t.intValue()).findFirst().orElse(null);
                    if(Objects.nonNull(sportIdVo)){
                        sb.append(sportIdVo.getSportName()+",");
                    }
                });
                if(sb.length() > 0){
                    afterVal =sb.substring(0,sb.length()-1);
                }
            }
            if(oldSports.size() == sportIdVoList.size()){
                beforeVal=SPORT_ALL;
            }
            if(newSports.size() == sportIdVoList.size()){
                afterVal=SPORT_ALL;
            }
            list.add(setBusinessLimitLog(rcsLabelLimitConfig, CommonUtil.Sport_ID, beforeVal, afterVal));
        }

        if (item.getBetExtraDelay() != rcsLabelLimitConfig.getBetExtraDelay()){
            String beforeVal ="";
            if(Objects.nonNull(rcsLabelLimitConfig.getBetExtraDelay())){
                beforeVal=rcsLabelLimitConfig.getBetExtraDelay()+"";
            }
            String afterVal ="";
            if(Objects.nonNull(item.getBetExtraDelay())){
                afterVal=item.getBetExtraDelay()+"";
            }
            log.info("::{}::{} 原值{}，修改值{}",rcsLabelLimitConfig.getTagId(),CommonUtil.Bet_Extra_Delay,beforeVal,afterVal);
            list.add(setBusinessLimitLog(rcsLabelLimitConfig,CommonUtil.Bet_Extra_Delay,beforeVal, afterVal));
        }
        if (!item.getTagMarketLevelId().equals(rcsLabelLimitConfig.getTagMarketLevelId())){
            String beforeVal=setOddsByMarketId(rcsLabelLimitConfig.getTagMarketLevelId());
            String afterVal=setOddsByMarketId(item.getTagMarketLevelId());
            list.add(setBusinessLimitLog(rcsLabelLimitConfig,CommonUtil.Tag_Market_Level_Id, beforeVal, afterVal));
        }
        if (!item.getSpecialBettingLimit().equals(rcsLabelLimitConfig.getSpecialBettingLimit())){

            String oldLimitVolumePercentage = "";
            if(Objects.nonNull(rcsLabelLimitConfig.getLimitPercentage())){
                oldLimitVolumePercentage =rcsLabelLimitConfig.getLimitPercentage().intValue()+SPORT_PERCENTAGE;
            }
            String newLimitVolumePercentage = "";
            if(Objects.nonNull(item.getLimitPercentage())){
                newLimitVolumePercentage =item.getLimitPercentage().intValue()+SPORT_PERCENTAGE;
            }

            String beforeVal = rcsLabelLimitConfig.getSpecialBettingLimit() == null ? LOG_IS_NO : rcsLabelLimitConfig.getSpecialBettingLimit() == 0 ? LOG_IS_NO : oldLimitVolumePercentage;
            String afterVal = item.getSpecialBettingLimit() == null ? LOG_IS_NO : item.getSpecialBettingLimit() ==0 ? LOG_IS_NO : newLimitVolumePercentage;
            list.add(setBusinessLimitLog(rcsLabelLimitConfig,CommonUtil.Special_Betting_Limit, beforeVal, afterVal));
        }
        if (Objects.nonNull(item.getLimitPercentage())){
            String beforeVal =  "";
            String afterVal =  "";
            afterVal= item.getLimitPercentage().intValue()+SPORT_PERCENTAGE;
            if(Objects.isNull(rcsLabelLimitConfig.getLimitPercentage())){
                list.add(setBusinessLimitLog(rcsLabelLimitConfig,CommonUtil.Limit_Percentage, beforeVal, afterVal));
            }else{
                if(item.getLimitPercentage().compareTo(rcsLabelLimitConfig.getLimitPercentage()) != 0){
                    beforeVal= rcsLabelLimitConfig.getLimitPercentage().intValue()+SPORT_PERCENTAGE;
                    list.add(setBusinessLimitLog(rcsLabelLimitConfig,CommonUtil.Limit_Percentage, beforeVal, afterVal));
                }
            }
        }
        if (Objects.nonNull(item.getExtraMargin())){
            String beforeVal =  "";
            String afterVal =  "";
            afterVal = item.getExtraMargin().intValue()+SPORT_PERCENTAGE;
            if(Objects.isNull(rcsLabelLimitConfig.getExtraMargin())){
                list.add(setBusinessLimitLog(rcsLabelLimitConfig,CommonUtil.Extra_Margin, beforeVal, afterVal));
            }else{
                if(item.getExtraMargin().compareTo(rcsLabelLimitConfig.getExtraMargin()) != 0){
                    beforeVal= rcsLabelLimitConfig.getExtraMargin().intValue()+SPORT_PERCENTAGE;
                    list.add(setBusinessLimitLog(rcsLabelLimitConfig,CommonUtil.Extra_Margin, beforeVal, afterVal));
                }
            }
        }
        return list;
    }
    /**
     * 新增标签日志
     * */
    private List<RcsQuotaBusinessLimitLog> setBusinessLimitLog(RcsLabelLimitConfig item,List<Integer> newSports){
        List<RcsQuotaBusinessLimitLog> list=new ArrayList<>();
        if(Objects.nonNull(item.getBetExtraDelay()) && item.getBetExtraDelay() >0){
            list.add(setBusinessLimitLog(item,CommonUtil.Bet_Extra_Delay, log_Insert, item.getBetExtraDelay()+""));
        }
        if(Objects.nonNull(newSports)) {
            List<SportIdVo> sportIdVoList =CommonUtil.setBusiness();
            String afterVal =  "";
            StringBuilder builder=new StringBuilder();
            newSports.forEach(t->{
                SportIdVo sportIdVo=sportIdVoList.stream().filter(s->s.getSportId() == t.intValue()).findFirst().orElse(null);
                if(Objects.nonNull(sportIdVo)){
                    builder.append(sportIdVo.getSportName()+",");
                }
            });
            if(builder.length() >0){
                afterVal =builder.substring(0,builder.length()-1);
            }
            if(StringUtils.isNotEmpty(afterVal)){
                if(newSports.size() == CommonUtil.setBusiness().size()){
                    afterVal=SPORT_ALL;
                }
                list.add(setBusinessLimitLog(item, CommonUtil.Sport_ID, log_Insert, afterVal));
            }
        }
        if(StringUtils.isNotEmpty(item.getTagMarketLevelId())) {
            String afterVal=setOddsByMarketId(item.getTagMarketLevelId());
            list.add(setBusinessLimitLog(item, CommonUtil.Tag_Market_Level_Id, log_Insert, afterVal));
        }
        if(Objects.nonNull(item.getSpecialBettingLimit()) && item.getSpecialBettingLimit() > 0) {
            String newLimitVolumePercentage = "";
            if(Objects.nonNull(item.getLimitPercentage())){
                newLimitVolumePercentage =item.getLimitPercentage().intValue()+"";
            }
            String beforeVal = log_Insert;
            String afterVal = item.getSpecialBettingLimit() == null ? LOG_IS_NO : item.getSpecialBettingLimit() ==0 ? LOG_IS_NO: newLimitVolumePercentage;
            list.add(setBusinessLimitLog(item, CommonUtil.Special_Betting_Limit, beforeVal, afterVal));
        }
//        if(Objects.nonNull(item.getVolumePercentage()) && item.getVolumePercentage().compareTo(BigDecimal.ZERO) ==1) {
//            list.add(setBusinessLimitLog(item, CommonUtil.Limit_Percentage, log_Insert, item.getVolumePercentage().intValue() + ""));
//        }
//        if(Objects.nonNull(item.getLimitPercentage()) && item.getLimitPercentage().compareTo(BigDecimal.ZERO) ==1) {
//            list.add(setBusinessLimitLog(item, CommonUtil.Limit_Percentage, log_Insert, item.getLimitPercentage().intValue() + ""));
//        }
        if(Objects.nonNull(item.getExtraMargin()) && item.getExtraMargin().compareTo(BigDecimal.ZERO) ==1) {
          list.add(setBusinessLimitLog(item, CommonUtil.Extra_Margin, log_Insert, item.getExtraMargin().intValue() + ""));
        }
        return list;
    }
    /**
     * 设置倍率分组
     * */
    private String setOddsByMarketId(String marketId){
        String marketValue = "";
        switch (marketId){
            case "10":
                marketValue = "0";
                break;
            case "11":
                marketValue = "1";
                break;
            case "12":
                marketValue = "2";
                break;
            case "13":
                marketValue = "3";
                break;
            case "14":
                marketValue = "4";
                break;
            case "15":
                marketValue = "5";
                break;
            default:
                marketValue=marketId;
                break;
        }
        return marketValue;
    }

}
