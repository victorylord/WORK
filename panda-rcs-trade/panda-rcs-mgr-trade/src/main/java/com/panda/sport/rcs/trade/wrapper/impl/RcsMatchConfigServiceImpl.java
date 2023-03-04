package com.panda.sport.rcs.trade.wrapper.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.mapper.RcsMatchConfigMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchConfig;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.trade.wrapper.RcsMatchConfigService;
import com.panda.sport.rcs.vo.MatchStatusAndDataSuorceVo;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-11-08 20:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Service
public class RcsMatchConfigServiceImpl extends ServiceImpl<RcsMatchConfigMapper, RcsMatchConfig> implements RcsMatchConfigService {
    @Autowired
    RcsMatchConfigMapper rcsMatchConfigMapper;
    
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    
    @Override
    public RcsMatchConfig selectMatchConfig(Long matchId) {
        QueryWrapper<RcsMatchConfig> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsMatchConfig::getMatchId, matchId);
        return getOne(wrapper);
    }

    @Override
    public List<RcsMatchConfig> selectMatchConfigByMatchIds(List<Long> ids) {
        return rcsMatchConfigMapper.selectMatchConfigByMatchIds(ids);
    }

    @Override
    public Integer getTradeType(Long matchId) {
        QueryWrapper<RcsMatchConfig> rcsMatchConfigQueryWrapper = new QueryWrapper<>();
        rcsMatchConfigQueryWrapper.lambda().select(RcsMatchConfig::getTradeType).last("limit 1");
        rcsMatchConfigQueryWrapper.lambda().eq(RcsMatchConfig::getMatchId,matchId);
        RcsMatchConfig rcsMatchConfig = rcsMatchConfigMapper.selectOne(rcsMatchConfigQueryWrapper);
        if(rcsMatchConfig==null){
            return 0;
        }
        return rcsMatchConfig.getTradeType();
    }

    @Override
    public void insert(RcsMatchConfig rcsMatchConfig) {
        rcsMatchConfigMapper.insert(rcsMatchConfig);
    }

    @Override
    public void updateRcsMatchConfig(RcsMatchConfig rcsMatchConfig) {
        UpdateWrapper<RcsMatchConfig> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("match_id", rcsMatchConfig.getMatchId());
        rcsMatchConfigMapper.update(rcsMatchConfig, updateWrapper);
    }

    @Override
    public void updateRiskManagerCode(Map<String,Object> map){
        rcsMatchConfigMapper.updateRiskManagerCode(map);
        
        RcsTradeConfig rcsTradeConfig = new RcsTradeConfig();
        rcsTradeConfig.setMatchId(String.valueOf(map.get("matchId")));
        rcsTradeConfig.setRiskManagerCode(String.valueOf(map.get("set_risk_manager_code")));
        producerSendMessageUtils.sendMessage(MqConstants.TRADE_CONFIG_CHANGE, 
        		"risk_manager_code", String.valueOf(map.get("matchId")), rcsTradeConfig);
        
        MatchStatusAndDataSuorceVo matchStatusAndDataSuorceVo = new MatchStatusAndDataSuorceVo();
        matchStatusAndDataSuorceVo.setMatchId(Long.parseLong(String.valueOf(map.get("matchId"))));
        matchStatusAndDataSuorceVo.setRiskManagerCode(String.valueOf(map.get("set_risk_manager_code")));
        producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, "", 
        		matchStatusAndDataSuorceVo);
        
    }
}
