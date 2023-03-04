package com.panda.sport.rcs.trade.wrapper.tourTemplate.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.vo.TournamentTemplatePlayMargainVo;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.tourTemplate
 * @Description :  联赛模板玩法margain配置
 * @Date: 2020-05-10 20:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsTournamentTemplatePlayMargainServiceImpl extends ServiceImpl<RcsTournamentTemplatePlayMargainMapper, RcsTournamentTemplatePlayMargain> implements IRcsTournamentTemplatePlayMargainService {
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;

    @Override
    public RcsTournamentTemplatePlayMargain getMatchPlayTemplateConfig(Long matchId, Integer playId) {
        return this.baseMapper.getMatchPlayTemplateConfig(matchId, playId);
    }
    /**
     * @Description   //获取联赛盘口差、水差变化率、赔率变化
     * @Param [config]
     * @Author  Sean
     * @Date  13:38 2020/10/20
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    @Override
    public RcsTournamentTemplatePlayMargain getRcsTournamentTemplateConfig(RcsMatchMarketConfig config) {
        if (NumberUtils.INTEGER_TWO.intValue() == config.getMatchType()) {
            config.setMatchType(NumberUtils.INTEGER_ZERO);
        }
        // 1.查询变化幅度
        RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
        if (ObjectUtils.isEmpty(rcsTournamentTemplatePlayMargin) ||
                ObjectUtils.isEmpty(rcsTournamentTemplatePlayMargin.getMarketAdjustRange())) {

            log.error("::{}::没有找到联赛配置={}",config.getMatchId(), JSONObject.toJSONString(config));
            throw new RcsServiceException("没有找到联赛配置");
        }
        return rcsTournamentTemplatePlayMargin;
    }

    @Override
    public Map<String, String> queryDataSource(List<Long> matchIds) {
        Map<String, String> dataSourceMap = new HashMap();
        List<TournamentTemplatePlayMargainVo> playMargains = baseMapper.queryDataSource(matchIds);
        if(!CollectionUtils.isEmpty(playMargains)){
           dataSourceMap = playMargains.stream().filter(filter-> StringUtils.isNotBlank(filter.getDataSource())).collect(Collectors.toMap(e -> e.getMatchId() + "_" + e.getPlayId(), e -> e.getDataSource()));
        }
        return dataSourceMap ;
    }

    @Override
    public Map<Long, String> queryDataSource(Long matchId) {
        List<TournamentTemplatePlayMargainVo> list = baseMapper.queryDataSource(Lists.newArrayList(matchId));
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().filter(filter -> StringUtils.isNotBlank(filter.getDataSource())).collect(Collectors.toMap(e -> e.getPlayId().longValue(), TournamentTemplatePlayMargainVo::getDataSource));
    }
}
