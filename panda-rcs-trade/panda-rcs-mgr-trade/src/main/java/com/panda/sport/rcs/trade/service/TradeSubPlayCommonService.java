package com.panda.sport.rcs.trade.service;

import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.service.impl.TradeModeServiceImpl;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

/**
 * @Description //操盘的一些校验
 * @Param
 * @Author sean
 * @Date 2021/1/9
 * @return
 **/
@Service
@Slf4j
public class TradeSubPlayCommonService {
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private TradeModeServiceImpl tradeModeService;

    public void setSubPlayId(RcsMatchMarketConfig config) {
        if (StringUtils.isBlank(config.getSubPlayId())){
            if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()) ||
                TradeConstant.BASKETBALL_X_PLAYS.contains(config.getPlayId().intValue()) ||
                TradeConstant.FOOTBALL_X_NO_INSERT_PLAYS.contains(config.getPlayId().intValue()) ||
                    !(SportIdEnum.isFootball(config.getSportId()) || SportIdEnum.isBasketball(config.getSportId()))){
                StandardSportMarket market = standardSportMarketMapper.selectById(config.getMarketId());
                if (!ObjectUtils.isEmpty(market)){
                    if (!ObjectUtils.isEmpty(market.getChildMarketCategoryId())){
                        config.setSubPlayId(market.getChildMarketCategoryId().toString());
                    }else {
                        config.setSubPlayId(SubPlayUtil.getRongHeSubPlayId(market));
                    }
                }else {
                    config.setSubPlayId(config.getPlayId().toString());
                }
            }else {
                config.setSubPlayId(config.getPlayId().toString());
            }
        }
    }
    public void hasSubPlayId(RcsMatchMarketConfig config){
        if (TradeConstant.BASKETBALL_X_PLAYS.contains(config.getPlayId().intValue()) ||
            TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()) ||
                (!Tennis.isExistPlay(config.getPlayId()) && !PingPong.isExistPlay(config.getPlayId())  && !IceHockey.isExistPlay(config.getPlayId())) &&
                (!ObjectUtils.isEmpty(config.getSportId()) && (!(SportIdEnum.isFootball(config.getSportId()) || SportIdEnum.isBasketball(config.getSportId()))))){
            if (ObjectUtils.isEmpty(config.getSubPlayId())){
                throw new RcsServiceException("参数玩法需要子玩法id");
            }
        }else {
            config.setSubPlayId(config.getPlayId().toString());
        }
    }

    public static void main(String[] args) {
        TradeSubPlayCommonService   service = new TradeSubPlayCommonService();
        RcsMatchMarketConfig    config = new RcsMatchMarketConfig();
        for (Long aLong : Arrays.asList(153L,154L,155L,202L)) {
            config.setPlayId(aLong);
            config.setSportId(5);
            service.hasSubPlayId(config);
        }

    }

    /**
     * @Description   //切换操盘方式
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return void
     **/
    public void changeTradeType(RcsMatchMarketConfig config,Long sportId) {
        // 切换操盘方式不推送赔率
        MarketStatusUpdateVO vo = new MarketStatusUpdateVO();
        vo.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
        vo.setSportId(sportId);
        vo.setMatchId(config.getMatchId());
        vo.setCategoryId(config.getPlayId());
        vo.setTradeType(TradeEnum.MANUAD.getCode());
        vo.setUpdateUserId(TradeUserUtils.getUserIdNoException());
        vo.setLinkedType(LinkedTypeEnum.NEW_MARKET.getCode());
        vo.setNewFlag(YesNoEnum.Y.getValue());
        vo.setOperateSource(YesNoEnum.Y.getValue());
        vo.setIsPushOdds(YesNoEnum.N.getValue());
        tradeModeService.updateTradeMode(vo);
    }
}
