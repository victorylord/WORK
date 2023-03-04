package com.panda.sport.rcs.trade.service.api.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.pojo.dto.UserExceptionDTO;
import com.panda.sport.rcs.pojo.vo.LogData;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.RcsUserException;
import com.panda.sport.rcs.vo.RcsUserExceptionVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.mapper.TUserBetRateMapper;
import com.panda.sport.rcs.pojo.RcsTradeRestrictMerchantSetting;
import com.panda.sport.rcs.pojo.RcsUserSpecialBetLimitConfig;
import com.panda.sport.rcs.pojo.TUserBetRate;
import com.panda.sport.rcs.pojo.dto.api.RcsTradeRestrictMerchantSettingDto;
import com.panda.sport.rcs.pojo.dto.api.UserIdDto;
import com.panda.sport.rcs.pojo.dto.api.UserSpecialLimitDto;
import com.panda.sport.rcs.trade.service.api.UserLimitApiService;
import com.panda.sport.rcs.trade.vo.RcsUserConfigVo;
import com.panda.sport.rcs.trade.vo.RcsUserSpecialBetLimitConfigDataVo;
import com.panda.sport.rcs.trade.vo.RcsUserSpecialBetLimitConfigsVo;
import com.panda.sport.rcs.trade.wrapper.RcsTradeRestrictMerchantSettingService;
import com.panda.sport.rcs.trade.wrapper.RcsUserConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsUserSpecialBetLimitConfigService;
import com.panda.sports.api.vo.ShortSysUserVO;

import lombok.extern.slf4j.Slf4j;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 用户限额服务
 * @Author : Paca
 * @Date : 2021-08-18 13:35
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class UserLimitApiServiceImpl implements UserLimitApiService {

    @Autowired
    private RcsUserConfigService rcsUserConfigService;
    
    @Autowired
    private RcsUserSpecialBetLimitConfigService rcsUserSpecialBetLimitConfigService;

    @Autowired
    private TUserBetRateMapper userBetRateMapper;
    
    @Autowired
    private RcsTradeRestrictMerchantSettingService rcsTradeRestrictMerchantSettingService;

    @Autowired
    private RcsOperationLogMapper rcsOperationLogMapper;

    @Override
    public Response<Boolean> updateUserSpecialLimit(Request<UserSpecialLimitDto> request) {
        log.info("::{}::修改用户特殊限额：" + JSON.toJSONString(request), CommonUtil.getRequestId());
        if (request == null) {
            return Response.error(Response.FAIL, "request参数不能为空", false);
        }
        UserSpecialLimitDto userSpecialLimitDto = request.getData();
        if (userSpecialLimitDto == null) {
            return Response.error(Response.FAIL, "data参数不能为空", false);
        }
        Long userId = userSpecialLimitDto.getUserId();
        BigDecimal percentage = userSpecialLimitDto.getPercentage();
        if (userId == null) {
            return Response.error(Response.FAIL, "data参数不能为空", false);
        }
        
        // 投注额外延时
        Integer betExtraDelay = userSpecialLimitDto.getBetExtraDelay();
        // 特殊限额类型
        Integer specialLimitType = userSpecialLimitDto.getSpecialLimitType();
        // 赔率分组
        String tagMarketLevelId = userSpecialLimitDto.getTagMarketLevelId();
  
        // 当前用户的原有配置
        Map<Long, RcsUserConfigVo> userConfigMap = rcsUserConfigService.getRcsUserConfigVo(Lists.newArrayList(userId));
        RcsUserConfigVo rcsUserConfig = userConfigMap.get(userId);
        if (rcsUserConfig == null) {
            rcsUserConfig = new RcsUserConfigVo();
            rcsUserConfig.setUserId(userId);
        }
        if (StringUtils.isNotEmpty(tagMarketLevelId)) {
        	rcsUserConfig.setTagMarketLevelId(tagMarketLevelId);
        }
        // 特殊投注延时,设置赛种
        if (betExtraDelay != null) {
        	rcsUserConfig.setBetExtraDelay(betExtraDelay);
        	rcsUserConfig.setSportIdList(userSpecialLimitDto.getSportIdList());
        } else {
        	if (CollectionUtils.isEmpty(rcsUserConfig.getSportIdList())) {
        		rcsUserConfig.setSportIdList(Lists.newArrayList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        	}
        }
        
        String remark = userSpecialLimitDto.getRemark();
        // 设置默认备注
        if (StringUtils.isBlank(remark) && specialLimitType != null) {
            remark = "业务修改用户特殊限额";
        }
        if (StringUtils.isBlank(remark) && betExtraDelay != null) {
        	remark = "业务修改用户投注额外延时";
        }
        if (StringUtils.isBlank(remark) && tagMarketLevelId != null) {
        	remark = "业务修改用户赔率分组";
        }
        rcsUserConfig.setRemarks(remark);
        
        RcsUserSpecialBetLimitConfigsVo configsVo = new RcsUserSpecialBetLimitConfigsVo();
        configsVo.setRcsUserConfigVo(rcsUserConfig);
        if (specialLimitType != null) {
        	rcsUserConfig.setSpecialBettingLimit(specialLimitType);
	        if (NumberUtils.INTEGER_TWO.equals(specialLimitType)) {
	            LambdaQueryWrapper<RcsUserSpecialBetLimitConfig> wrapper = Wrappers.lambdaQuery();
	            wrapper.eq(RcsUserSpecialBetLimitConfig::getUserId, userId)
	                    .eq(RcsUserSpecialBetLimitConfig::getSpecialBettingLimitType, specialLimitType)
	                    .eq(RcsUserSpecialBetLimitConfig::getStatus, 1).orderByDesc(RcsUserSpecialBetLimitConfig :: getUpdateTime)
	                    .last("LIMIT 1");
	            
	            RcsUserSpecialBetLimitConfig config = rcsUserSpecialBetLimitConfigService.getOne(wrapper);
	            if (config != null) {
	                config.setOldPercentageLimit(config.getPercentageLimit());
	                LambdaQueryWrapper<RcsUserSpecialBetLimitConfig> delWrapper = Wrappers.lambdaQuery();
	                delWrapper.eq(RcsUserSpecialBetLimitConfig::getUserId, userId)
	                        .eq(RcsUserSpecialBetLimitConfig::getSpecialBettingLimitType, specialLimitType);
	                rcsUserSpecialBetLimitConfigService.remove(delWrapper);
                    config.setId(null);
	            } else {
	                config = new RcsUserSpecialBetLimitConfig();
	            }
	            config.setUserId(userId);
	            config.setOrderType(null);
	            config.setSportId(null);
	            config.setStatus(1);
	            config.setSpecialBettingLimitType(specialLimitType);
	            config.setPercentageLimit(percentage);
	
	            RcsUserSpecialBetLimitConfigDataVo dataVo = new RcsUserSpecialBetLimitConfigDataVo();
	            dataVo.setSpecialBettingLimitType(specialLimitType);
	            dataVo.setRcsUserSpecialBetLimitConfigList1(Lists.newArrayList(config));
	
	            configsVo.setRcsUserSpecialBetLimitConfigDataVoList(Lists.newArrayList(dataVo));
	        }
        }
        int operatorId = NumberUtils.toInt(userSpecialLimitDto.getOperatorId());
        String operatorName = userSpecialLimitDto.getOperatorName();
        ShortSysUserVO traderData = new ShortSysUserVO();
        traderData.setId(operatorId);
        traderData.setUserCode(operatorName);
        configsVo.setTraderData(traderData);

        //1061需求 新增用户的风控措施数据
        LambdaQueryWrapper<TUserBetRate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TUserBetRate::getUserId, userId);
        List<TUserBetRate> tUserBetRates = userBetRateMapper.selectList(wrapper);

        if (!CollectionUtils.isEmpty(tUserBetRates)) {
            configsVo.setUserBetRateList(tUserBetRates);
        }

        log.info("::{}::修改用户特殊限额：" , CommonUtil.getRequestId(), JSON.toJSONString(configsVo));
        rcsUserConfigService.updateRcsUserSpecialBetLimitConfigsVo(configsVo, operatorId, false);
        return Response.success(true);
    }

	@Override
	public Response<RcsTradeRestrictMerchantSettingDto> getUserTradeRestrict(UserIdDto userIdDto) {
		RcsTradeRestrictMerchantSettingDto result = new RcsTradeRestrictMerchantSettingDto();
		// 查询该用户
		QueryWrapper<RcsTradeRestrictMerchantSetting> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(RcsTradeRestrictMerchantSetting :: getUserId, userIdDto.getUserId());
		RcsTradeRestrictMerchantSetting one = rcsTradeRestrictMerchantSettingService.getOne(queryWrapper);
		if (one != null) {
			BeanUtils.copyProperties(one, result);
		}
		 // 当前用户的原有配置
        Map<Long, RcsUserConfigVo> userConfigMap = rcsUserConfigService.getRcsUserConfigVo(Lists.newArrayList(userIdDto.getUserId()));
        RcsUserConfigVo rcsUserConfig = userConfigMap.get(userIdDto.getUserId());
        if (rcsUserConfig != null) {
        	List<Long> sportIdList = rcsUserConfig.getSportIdList();
        	result.setSportIds(Joiner.on(",").join(sportIdList));
        }
		return Response.success(result);
	}

    @Override
    public Response<RcsUserExceptionVo> queryUserExceptionByOnline(UserExceptionDTO req) {
        log.info("::{}::queryUserExceptionByOnline:输入参数{}", JSONObject.toJSONString(req),CommonUtil.getRequestId());
        if(Objects.isNull(req.getCategory())){
            req.setCategory(0);
        }
        RcsUserExceptionVo result=new RcsUserExceptionVo();
        List<RcsUserException> rcsUserExceptionList=new ArrayList<>();
        Integer outPutTotal=rcsOperationLogMapper.selectRcsOperationLogByOnLineCount(req);
        if(0==req.getCategory()){
            List<RcsUserException> groupList=rcsOperationLogMapper.selectRcsOperationLogByGroup(req);
            Integer total=groupList.size();
            int pageNum= req.getPageNum() * req.getPageSize();
            rcsUserExceptionList=groupList.stream().skip(pageNum).limit(req.getPageSize()).collect(Collectors.toList());
            List<RcsUserException> subList=rcsOperationLogMapper.selectRcsOperationLogByList(req);
            rcsUserExceptionList.forEach(item->{
                List<RcsUserException> rcsUserExceptions = subList.stream().filter(t->t.getUid().equals(item.getUid()) && t.getCrtTime().substring(0,10).equals(item.getCrtTime())).collect(Collectors.toList());
                for (RcsUserException rcsUserException : rcsUserExceptions){
                    if(rcsUserException.getId().equals(item.getId())){
                        continue;
                    }
                    if (item.getUpdateContents() == null) {
                        item.setUpdateContents(new ArrayList<>());
                    }
                    List<LogData> rcsLogDateList= JSONArray.parseArray(rcsUserException.getUpdateContent(),LogData.class);
                    List<LogData> itemLogDateList= JSONArray.parseArray(item.getUpdateContent(),LogData.class);
                    if(CollectionUtils.isEmpty(rcsLogDateList) || CollectionUtils.isEmpty(itemLogDateList)){
                        continue;
                    }
                    String value=null;
                    if(itemLogDateList.get(0).getName().length() < rcsLogDateList.get(0).getName().length()){
                        value=item.getUpdateContent();
                        item.setUpdateContent(rcsUserException.getUpdateContent());
                    }
                    if(StringUtils.isEmpty(value)){
                        item.getUpdateContents().add(rcsUserException.getUpdateContent());
                    }else{
                        item.getUpdateContents().add(value);
                    }
                }
                String orgValue=item.getMerchantCode()+"_";
                Pattern p=Pattern.compile(orgValue);
                Matcher m=p.matcher(item.getUserName());
                String userName=m.replaceAll("").trim();
                item.setUserName(userName.trim());
            });
            result.setOutPutTotal(outPutTotal);
            result.setTotal(total);
        }else{
            rcsUserExceptionList=rcsOperationLogMapper.selectRcsOperationLogByOnLine(req);
            rcsUserExceptionList.forEach(t->{
                String orgValue=t.getMerchantCode()+"_";
                Pattern p=Pattern.compile(orgValue);
                Matcher m=p.matcher(t.getUserName());
                String userName=m.replaceAll("").trim();
                t.setUserName(userName.trim());
            });
            result.setOutPutTotal(0);
            result.setTotal(0);
        }
        result.setRcsUserExceptions(rcsUserExceptionList);
        log.info("::{}::queryUserExceptionByOnline:输出参数总数{} 计算数 {}",CommonUtil.getRequestId(), result.getOutPutTotal(),result.getTotal());
        return Response.success(result);
    }

}
