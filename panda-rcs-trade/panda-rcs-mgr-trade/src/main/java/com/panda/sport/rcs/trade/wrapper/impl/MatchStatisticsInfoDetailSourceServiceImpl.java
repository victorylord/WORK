package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.MatchStatisticsInfoDetailSourceMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetailSource;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoDetailSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Felix
 */
@Slf4j
@Service
public class MatchStatisticsInfoDetailSourceServiceImpl extends ServiceImpl<MatchStatisticsInfoDetailSourceMapper, MatchStatisticsInfoDetailSource> implements MatchStatisticsInfoDetailSourceService {

    @Autowired
    private MatchStatisticsInfoDetailSourceMapper matchStatisticsInfoDetailSourceMapper;

    @Override
    public List<MatchStatisticsInfoDetailSource> getThirdMatchScoreList(Long matchId, String dataSourceCode) {
        QueryWrapper<MatchStatisticsInfoDetailSource> matchStatisticsInfoDetailQueryWrapper = new QueryWrapper<>();
        matchStatisticsInfoDetailQueryWrapper.lambda().eq(MatchStatisticsInfoDetailSource::getStandardMatchId, matchId);
        matchStatisticsInfoDetailQueryWrapper.lambda().eq(MatchStatisticsInfoDetailSource::getDataSourceCode, dataSourceCode);
        List<MatchStatisticsInfoDetailSource> matchStatisticsInfoDetails = matchStatisticsInfoDetailSourceMapper.selectList(matchStatisticsInfoDetailQueryWrapper);
        return matchStatisticsInfoDetails;
    }


}
