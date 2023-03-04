package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.BalanceOptionEnum;
import com.panda.sport.rcs.enums.BalanceTypeEnum;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketMarginConfigMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.RcsUserRemarkRemindLog;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.odds.BalanceReqVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.MarketBalanceVo;
import com.panda.sport.rcs.trade.wrapper.IRcsUserRemarkRemindLogService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.vo.RcsUserRemarkRemindLogQueryVo;
import com.panda.sport.rcs.vo.statistics.BalanceVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 人工提醒日志逻辑
 * @Author : SkyKong
 * @Date : 2023-1-28 10:51
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class UserRemarkRemindLogService {

    @Autowired
    IRcsUserRemarkRemindLogService rcsUserRemarkRemindLogService;
    /**
     * 查询记录
     * */
    public Page<RcsUserRemarkRemindLog>  getUserRemarkRemindLog(RcsUserRemarkRemindLogQueryVo param){
        Page<RcsUserRemarkRemindLog> page = new Page<>(param.getCurrentPage(), param.getPageSize());
        LambdaQueryWrapper<RcsUserRemarkRemindLog> qw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(param.getUsername())) {
            qw.and(e -> e.likeRight(RcsUserRemarkRemindLog::getUserId, param.getUsername())
                    .or().likeRight(RcsUserRemarkRemindLog::getUsername, param.getUsername())
            );
        }
        qw.in(!param.getMerchantCode().isEmpty(), RcsUserRemarkRemindLog::getMerchantCode, param.getMerchantCode());
        qw.eq(StringUtils.isNotBlank(param.getCreateUserName()), RcsUserRemarkRemindLog::getCreateUserName, param.getCreateUserName());
        qw.eq(StringUtils.isNotBlank(param.getRemindDate()), RcsUserRemarkRemindLog::getRemindDate, param.getRemindDate());
        qw.gt(StringUtils.isNotBlank(param.getStartCreateTime()), RcsUserRemarkRemindLog::getCreateTime, param.getStartCreateTime());
        qw.le(StringUtils.isNotBlank(param.getEndCreateTime()), RcsUserRemarkRemindLog::getCreateTime, param.getEndCreateTime());
        qw.orderByDesc(RcsUserRemarkRemindLog::getCreateTime);
        page = rcsUserRemarkRemindLogService.page(page, qw);
        return  page;
    }
    /**
     * 人工提醒更新
     * */
    public  void updateRemark(RcsUserRemarkRemindLog rcsUserRemarkRemindLog,Integer traderId){
        rcsUserRemarkRemindLogService.updateRemark(rcsUserRemarkRemindLog,traderId);
    }
    /**
     * 获取列表记录
     * */
    public List<RcsUserRemarkRemindLog> getList(String userId){
      return   rcsUserRemarkRemindLogService.list(
                new LambdaQueryWrapper<RcsUserRemarkRemindLog>()
                        .eq(RcsUserRemarkRemindLog::getUserId, userId).orderByDesc(RcsUserRemarkRemindLog::getId));
    }
    /**
     * 获取记录数
     * */
    public long getCount(String userId){
        return  rcsUserRemarkRemindLogService.count(
                new LambdaQueryWrapper<RcsUserRemarkRemindLog>()
                        .eq(RcsUserRemarkRemindLog::getUserId, userId).orderByDesc(RcsUserRemarkRemindLog::getId));
    }
   /**
    * 获取单个记录
    * */
    public RcsUserRemarkRemindLog getOne(String userId){
        return  rcsUserRemarkRemindLogService.getOne(
                new LambdaQueryWrapper<RcsUserRemarkRemindLog>()
                        .eq(RcsUserRemarkRemindLog::getUserId, userId).orderByDesc(RcsUserRemarkRemindLog::getId).last("limit 1"));
    }
}
