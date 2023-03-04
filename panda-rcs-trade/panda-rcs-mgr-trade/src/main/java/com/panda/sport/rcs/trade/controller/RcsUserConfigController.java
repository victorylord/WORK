package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.pojo.RcsOperationLog;
import com.panda.sport.rcs.pojo.RcsUserConfigNew;
import com.panda.sport.rcs.pojo.TUser;
import com.panda.sport.rcs.pojo.TUserBetRate;
import com.panda.sport.rcs.pojo.dto.UserHistoryReqVo;
import com.panda.sport.rcs.pojo.vo.LogData;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.*;
import com.panda.sport.rcs.trade.wrapper.IRcsUserConfigNewService;
import com.panda.sport.rcs.trade.wrapper.RcsUserConfigService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsOperationLogHistory;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * 用户配置
 * @create: 2020-12-08 16:07
 **/
@Component
@RestController
@RequestMapping(value = "/rcsUserConfig")
@Slf4j
public class RcsUserConfigController {
    @Autowired
    private RcsUserConfigMapper rcsUserConfigMapper;
    @Autowired
    private RcsUserConfigService rcsUserConfigService;
    @Autowired
    private RcsUserSpecialBetLimitConfigMapper rcsUserSpecialBetLimitConfigMapper;
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private TUserBetRateMapper userBetRateMapper;


    @Autowired
    private IRcsUserConfigNewService userConfigNewService;

    @Autowired
    private RcsUserConfigNewMapper userConfigNewMapper;

    @Autowired
    private TUserMapper userMapper;
    @Autowired
    private RcsOperationLogMapper rcsOperationLogMapper;
    private String Hkey1 = "%s_%s_%s_single_note_claim_limit";
    private String Hkey2 = "%s_%s_%s_single_game_claim_limit";
    /**
     * 延迟数据干掉
     */
    private String SPECIAL_USER_CONFIG = "rcs:special:user:order:delay:config:%s";

    /**
     * 获取用户配置数据
     *
     * @param rcsUserConfigDTO
     * @return
     */
    @RequestMapping(value = "/getRcsUserConfig", method = RequestMethod.POST)
    private HttpResponse<Collection<RcsUserConfigVo>> getRcsUserConfig(@RequestBody RcsUserConfigDTO rcsUserConfigDTO) {
        try {
            HashMap<Long, RcsUserConfigVo> rcsUserConfigVo = rcsUserConfigService.getRcsUserConfigVo(rcsUserConfigDTO.getUserIdList());
            if (!CollectionUtils.isEmpty(rcsUserConfigVo)) {
                return HttpResponse.success(rcsUserConfigVo.values());
            }
            return HttpResponse.success();
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    private HttpResponse<RcsUserSpecialBetLimitConfigVo> getList(Long userId) {
        try {
            if (userId == null) {
                return HttpResponse.failToMsg("用户id不能为空");
            }
            /*
            * bug 181 临时 兼容处理
            * */
            TUser user = userMapper.selectByUserId(userId);
            int level = user.getUserLevel();

            LambdaQueryWrapper<RcsUserConfigNew> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(RcsUserConfigNew::getUserId, userId);
            RcsUserConfigNew configNew = userConfigNewMapper.selectOne(lambdaQueryWrapper);
            String tagMarketLevelId = "";
            if (null != configNew && null != configNew.getTagMarketLevelId()) {
                tagMarketLevelId = configNew.getTagMarketLevelId();
            }
            //用作开关
            String rediskey = "rcs:user_market_level_status";
            if (StringUtils.isEmpty(redisUtils.get(rediskey)) && (level == 208 ||level == 234 ||level == 230) && tagMarketLevelId.equals("12")) {
                LambdaUpdateWrapper<RcsUserConfigNew> newConfigLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                newConfigLambdaUpdateWrapper.eq(RcsUserConfigNew::getUserId, userId);
                newConfigLambdaUpdateWrapper.set(RcsUserConfigNew::getTagMarketLevelId, null);
                userConfigNewService.update(newConfigLambdaUpdateWrapper);
                log.info("::{}::用户赔率分组恢复", userId);
            }
            RcsUserSpecialBetLimitConfigVo rcsUserSpecialBetLimitConfigVo = rcsUserConfigService.getList(userId);

            IPage<RcsOperationLog> iPage = new Page<>(1, 100);
            IPage<RcsOperationLog> iPage1 = rcsOperationLogMapper.selectRcsOperationLog(iPage, String.valueOf(userId));
            List<RcsOperationLog> rcsOperationLogs = iPage1.getRecords();
            if (!CollectionUtils.isEmpty(rcsOperationLogs)) {
                for (RcsOperationLog rcsOperationLog : rcsOperationLogs) {
                    rcsOperationLog.setUpdateContentList(JSONObject.parseArray(rcsOperationLog.getUpdateContent(), LogData.class));
                    rcsOperationLog.setUpdateContent(null);
                    rcsOperationLog.setCrtTimeDate(getDate(rcsOperationLog.getCrtTime()));
                }
            }
            RcsOperationLogVo rcsOperationLogVo = new RcsOperationLogVo();
            rcsOperationLogVo.setTotal((int) iPage1.getTotal());
            rcsOperationLogVo.setRcsOperationLogList(rcsOperationLogs);
            rcsUserSpecialBetLimitConfigVo.setRcsOperationLogVo(rcsOperationLogVo);

            //1061需求 新增用户的风控措施数据
            LambdaQueryWrapper<TUserBetRate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TUserBetRate::getUserId, userId);
            List<TUserBetRate> tUserBetRates = userBetRateMapper.selectList(wrapper);
            rcsUserSpecialBetLimitConfigVo.setUserBetRateList(tUserBetRates);
            log.info("::{}::特殊管控查询用户:{},具体值为:{}", CommonUtil.getRequestId(),userId, JSONObject.toJSONString(rcsUserSpecialBetLimitConfigVo));
            return HttpResponse.success(rcsUserSpecialBetLimitConfigVo);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    private HttpResponse update(@RequestBody RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo) {
        try {
            Integer userId = TradeUserUtils.getUserId();
            return rcsUserConfigService.updateRcsUserSpecialBetLimitConfigsVo(rcsUserSpecialBetLimitConfigsVo, userId, true);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }
    @RequestMapping(value = "/getHistory", method = RequestMethod.GET)
    private HttpResponse getHistory(@RequestParam(required = false) String user, @RequestParam(required = false) List<String> merchantCodes, @RequestParam(required = false) String type, @RequestParam(required = false) Long startTime,@RequestParam(required = false) Long endTime, @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize) {
        UserHistoryReqVo req=new UserHistoryReqVo();
        req.setUser(user);
        try {
            if (StringUtils.isNotBlank(type)) {
                req.setTypes(Arrays.asList(type.split("-")));
            }
            if (pageSize == null) {
                req.setPageSize(30);
            }else{
                req.setPageSize(pageSize);
            }
            if(pageNum==null || pageNum==0){
                req.setPageNum(0);
            }else{
                req.setPageNum((pageNum*pageSize) - pageSize);
            }
            if(!CollectionUtils.isEmpty(merchantCodes)){
                req.setMerchantCodes(merchantCodes);
            }
            req.setStartTime(startTime);
            req.setEndTime(endTime);
            log.info("getHistory 请求参数：{}:",JSONObject.toJSONString(req));
            List<RcsOperationLogHistory> rcsOperationLogHistories = rcsOperationLogMapper.selectRcsOperationLogByUserLimit(req);
            log.info("getHistory 返回结果:{}", rcsOperationLogHistories.size());
            Integer count = rcsOperationLogMapper.selectRcsOperationLogByUserLimitCount(req);
            if (count == null) {
                count = 0;
            }
            RcsOperationLogHistoryVo historyVo = new RcsOperationLogHistoryVo();
            historyVo.setTotal(Long.valueOf(count));//总数
            historyVo.setPageSize(Long.valueOf(req.getPageSize()));//每页条数
            Long totalPages = 1L;
            if (count % req.getPageSize() == 0) {
                totalPages = Long.valueOf(count / req.getPageSize());
            } else {
                totalPages = Long.valueOf(count / req.getPageSize()) + 1L;
            }
            historyVo.setPageNum(totalPages);//总页数

            if (!CollectionUtils.isEmpty(rcsOperationLogHistories)) {
                for (RcsOperationLogHistory rcsOperationLogHistory : rcsOperationLogHistories) {
                    List<LogData> logDataList = JSONObject.parseArray(rcsOperationLogHistory.getUpdateContent(), LogData.class);
                    rcsOperationLogHistory.setLogDataList(logDataList);
                    if (!CollectionUtils.isEmpty(logDataList)) {
                        for (LogData logData : logDataList) {
                            if (logData.getName().equals("操作人")) {
                                rcsOperationLogHistory.setTrader(logData.getData());
                                break;
                            }
                        }
                    }
                    rcsOperationLogHistory.setUpdateContent(null);
                }
            }
            //产品说暂时去掉 小红点展示
            //historyVo.setCurrentDayCount(rcsOperationLogMapper.getCurrentDayCount(user, types, time,likeUser));//当日总数
            historyVo.setRcsOperationLogHistoryList(rcsOperationLogHistories);
            log.info("getHistory 最后返回:{}", historyVo);
            return HttpResponse.success(historyVo);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    private long getDate(String date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = sdf.parse(date);
        return parse.getTime();
    }
}
