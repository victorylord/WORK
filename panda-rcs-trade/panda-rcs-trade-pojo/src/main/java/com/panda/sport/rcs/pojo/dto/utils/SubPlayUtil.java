package com.panda.sport.rcs.pojo.dto.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.enums.SportEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Description   //子玩法转换
 * @Param
 * @Author  sean
 * @Date   2021/7/6
 * @return
 **/
@Slf4j
public class SubPlayUtil {
    /**
     * @Description   //根据前端参数获取子玩法id
     * @Param [config]
     * @Author  sean
     * @Date   2021/7/6
     * @return java.lang.String
     **/
    public static String getWebSubPlayId(RcsMatchMarketConfig config){
        String subPlayId = config.getSubPlayId();
        if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()) ||
            TradeConstant.BASKETBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue())){
            subPlayId = getRongHeSubPlayId(config);
        }
        if (StringUtils.isBlank(subPlayId)){
            subPlayId = config.getPlayId().toString();
        }
        return subPlayId;
    }
    /**
     * @Description   //前端子玩法id转化成融合子玩法id
     * @Param [config]
     * @Author  sean
     * @Date   2021/7/6
     * @return java.lang.String
     **/
    public static String getRongHeSubPlayId(RcsMatchMarketConfig config){
        log.info("前端子玩法id转化成融合子玩法id config={}", JSONObject.toJSONString(config));
        String subPlayId = config.getSubPlayId();
        if (ObjectUtils.isEmpty(config.getMarketId()) && TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()) && subPlayId.indexOf("-") >= 0){
            String[] ids = config.getSubPlayId().split("-");
            Integer playId = config.getPlayId().intValue();
            if (TradeConstant.FOOTBALL_X_A3_PLAYS.contains(config.getPlayId().intValue())){
                playId = playId * 100;
                subPlayId = playId + (Double.parseDouble(ids[2]) / 15) + "";
            }else if (TradeConstant.FOOTBALL_X_A1_PLAYS.contains(config.getPlayId().intValue())){
                playId = playId * 100;
                subPlayId = playId + Double.parseDouble(ids[1]) + "";
            } else if (TradeConstant.FOOTBALL_X_A2_PLAYS.contains(config.getPlayId().intValue())){
                playId = playId * 100;
                subPlayId = playId + Double.parseDouble(ids[1]) + "";
            }
        }else if (ObjectUtils.isEmpty(config.getMarketId()) && TradeConstant.BASKETBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()) && subPlayId.indexOf("-") >= 0){
            String[] ids = config.getSubPlayId().split("-");
            Integer playId = config.getPlayId().intValue();
            if (TradeConstant.FOOTBALL_X_A2_PLAYS.contains(config.getPlayId().intValue()) ||
                    TradeConstant.BASKETBALL_X_SCORCE_PLAYS.contains(config.getPlayId().intValue())){
                playId = playId * 100;
                subPlayId = playId + Double.parseDouble(ids[1]) + "";
            } else if (TradeConstant.FOOTBALL_X_A1_A2_PLAYS.contains(config.getPlayId().intValue())){
                subPlayId = config.getPlayId() * 10000 + (Double.parseDouble(ids[1]) *100) + (Double.parseDouble(ids[2])) + "";
            }else if (TradeConstant.FOOTBALL_X_A2_A1_PLAYS.contains(config.getPlayId().intValue())){
                subPlayId = config.getPlayId() * 10000 + (Double.valueOf(ids[2]).intValue() *100) + (Double.parseDouble(ids[1])) + "";
            }
        }
        return new BigDecimal(subPlayId).intValue()+"";
    }

    /**
     * @Description   //跟盘口参数生产子玩法id
     * @Param [market]
     * @Author  sean
     * @Date   2021/7/7
     * @return java.lang.String
     **/
    public static String getRongHeSubPlayId(RcsStandardMarketDTO market){
        log.info("前端子玩法id转化成融合子玩法id market={}", JSONObject.toJSONString(market));
        String subPlayId = market.getMarketCategoryId().toString();
        if (TradeConstant.FOOTBALL_X_A1_PLAYS.contains(market.getMarketCategoryId().intValue())){
            subPlayId = market.getMarketCategoryId()*100 + Double.parseDouble(market.getAddition1())+"";
        }else  if (TradeConstant.FOOTBALL_X_A2_PLAYS.contains(market.getMarketCategoryId().intValue())){
            subPlayId = market.getMarketCategoryId()*100 + Double.parseDouble(market.getAddition2()) + "";
        }else if (TradeConstant.FOOTBALL_X_A3_PLAYS.contains(market.getMarketCategoryId().intValue())){
            subPlayId = market.getMarketCategoryId()*100  + (Double.parseDouble(market.getAddition3()) / 15) + "";
        }if (TradeConstant.FOOTBALL_X_A1_A2_PLAYS.contains(market.getMarketCategoryId().intValue())){
            subPlayId = market.getMarketCategoryId() * 10000 + (Double.parseDouble(market.getAddition1()) *100) + (Double.parseDouble(market.getAddition2())) + "";
        }else if (TradeConstant.FOOTBALL_X_A2_A1_PLAYS.contains(market.getMarketCategoryId().intValue())){
            subPlayId = market.getMarketCategoryId() * 10000 + (Double.parseDouble(market.getAddition2()) *100) + (Double.parseDouble(market.getAddition1())) + "";
        }else if (TradeConstant.BASKETBALL_X_SCORCE_PLAYS.contains(market.getMarketCategoryId().intValue())){
            subPlayId = market.getMarketCategoryId()*100 + Double.parseDouble(market.getAddition1())+"";
        }else if (TradeConstant.BASKETBALL_X_SCORCE_PLAYS.contains(market.getMarketCategoryId().intValue()) && !ObjectUtils.isEmpty(market.getChildStandardCategoryId())){
            subPlayId = market.getChildStandardCategoryId().toString();
        }
        // 1390需求新增15分钟玩法
        if (TradeConstant.FOOTBALL_X_A5_PLAYS.contains(market.getMarketCategoryId().intValue())){
            subPlayId = (market.getMarketCategoryId()*100 + Integer.parseInt(market.getAddition5().split(",")[1]) / 15 )+"";
        }
        return new BigDecimal(subPlayId).intValue()+"";
    }
    /**
     * @Description   //跟盘口参数生产子玩法id
     * @Param [market]
     * @Author  sean
     * @Date   2021/7/7
     * @return java.lang.String
     **/
    public static String getRongHeSubPlayId(StandardSportMarket market){
        RcsStandardMarketDTO m = JSONObject.parseObject(JSONObject.toJSONString(market),RcsStandardMarketDTO.class);
        return getRongHeSubPlayId(m);
    }
    /**
     * @Description   //解析子玩法获取盘和局的信息
     * @Param [subPlayId]
     * @Author  sean
     * @Date   2021/9/28
     * @return java.util.Map<java.lang.String,java.lang.Long>
     **/
    public static Map<String, Long> getTennisPlaysBySubPlayId(Long playId,String subPlayId) {
        Map<String, Long> map = Maps.newHashMap();
        Long play = Long.parseLong(subPlayId);
        map.put("playId",playId);
        if (playId.longValue() != play){
            // 盘 数
            Long pan = playId*100 +99;
            if (play > pan){
                // 局数
               Long ju =  play % 100;
               map.put("ju",ju);
               pan = (play % 10000) / 100;
               map.put("pan",pan);
            }else {
                pan =  play % 100;
                map.put("pan",pan);
            }
        }
        return map;
    }
}


