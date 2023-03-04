package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.RcsOrderSecondConfigMapper;
import com.panda.sport.rcs.pojo.RcsOrderSecondConfig;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.LogFormatService;
import com.panda.sport.rcs.trade.wrapper.SportMatchViewService;
import com.panda.sport.rcs.vo.OrderSecondConfigVo;
import com.panda.sport.rcs.trade.wrapper.RcsOrderSecondConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.RedisKey.*;

/**
 * @author :  carver
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2020-11-17 20:26
 */
@Service
@Slf4j
public class RcsOrderSecondConfigServiceImpl extends ServiceImpl<RcsOrderSecondConfigMapper, RcsOrderSecondConfig> implements RcsOrderSecondConfigService {

    @Autowired
    private RcsOrderSecondConfigMapper rcsOrderSecondConfigMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private LogFormatService logFormatService;

    @Override
    public List<OrderSecondConfigVo> queryOrderSecondConfig(OrderSecondConfigVo param) {
        return rcsOrderSecondConfigMapper.selectOrderSecondConfig(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderSecondConfig(OrderSecondConfigVo configVo) {
        List<OrderSecondConfigVo> playSetList = configVo.getPlaySetList();
        String userLevel = null;
        if (!CollectionUtils.isEmpty(configVo.getUserLevels())) {
            userLevel = configVo.getUserLevels().toString().substring(1).replace("]", "").replace(" ", "");
        }

        for (OrderSecondConfigVo vo : playSetList) {
            RcsOrderSecondConfig config = BeanCopyUtils.copyProperties(vo, RcsOrderSecondConfig.class);
            config.setMatchInfoId(configVo.getMatchInfoId());
            config.setSecondStatus(vo.getSecondStatus());
            config.setPlaySetId(vo.getPlaySetId());

            if (configVo.getBetAmount() != null) {
                config.setBetAmount(configVo.getBetAmount() * 100);
            }
            if (StringUtils.isNotBlank(userLevel)) {
                config.setUserLevel(userLevel);
            }
            config.setTrader(configVo.getTrader());
            config.setUid(configVo.getUid());
            Long time = System.currentTimeMillis();
            config.setCreateTime(time);
            config.setUpdateTime(time);
            rcsOrderSecondConfigMapper.insertOrderSecondConfig(config);

            String key = String.format(FREE_ORDER, configVo.getMatchInfoId(), vo.getPlaySetId());
            List<RcsOrderSecondConfig> configs = rcsOrderSecondConfigMapper.selectOrderSecond(config, (time - FREE_ORDER_UPDATE_TIME));
            if (!CollectionUtils.isEmpty(configs)) {
                List<RcsOrderSecondConfig> oneType = configs.stream().filter(filter -> null == filter.getBetAmount() && StringUtils.isBlank(filter.getUserLevel())).collect(Collectors.toList());
                String redisValue = "";
                if (CollectionUtils.isEmpty(oneType)) {
                    redisValue = "2";
                } else {
                    redisValue = "1";
                }

                if (NumberUtils.INTEGER_ONE.intValue() == vo.getSecondStatus()) {
                    log.info("::{}::开启一建秒接={}",CommonUtil.getRequestId(), JSONObject.toJSONString(config));
                    redisClient.setExpiry(key, redisValue, FREE_ORDER_TIME);
                } else if (NumberUtils.INTEGER_ZERO.intValue() == vo.getSecondStatus()) {
                    List<RcsOrderSecondConfig> cos = configs.stream().filter(filter -> NumberUtils.INTEGER_ONE.intValue() == filter.getSecondStatus()).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(cos)) {
                        log.info("::{}::{}关闭一建秒接={}", CommonUtil.getRequestId(), configVo.getTrader(), JSONObject.toJSONString(config));
                        redisClient.delete(key);
                    }
                }
            }
        }
        boolean isLog = false;
        String format = String.format(ORDER_SECOND_TRADER, configVo.getMatchInfoId(), configVo.getUid());
        if (configVo.getSecondStatus() == 1) {
            String value = redisClient.get(format);
            if (StringUtils.isBlank(value) || !value.equals(JsonFormatUtils.toJson(configVo))) {
                isLog = true;
                redisClient.setExpiry(format, JsonFormatUtils.toJson(configVo), 1800L);
            }
        } else {
            isLog = true;
            redisClient.delete(format);
        }

        //记录操作日志
        if (isLog) logFormatService.saveOrderSecondConfigLog(configVo);
    }

    @Override
    public List<String> selectOrderSecondTraders(OrderSecondConfigVo vo) {
        Long time = System.currentTimeMillis() - FREE_ORDER_UPDATE_TIME;
        List<String> traders = rcsOrderSecondConfigMapper.selectOrderSecondTraders(vo, time);
        return traders;
    }
}
