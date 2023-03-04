package com.panda.sport.rcs.trade.wrapper.impl;

import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.trade.wrapper.MatchSnapShotService;
import com.panda.sport.rcs.vo.OddsSnapShotVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2020-07-08 17:59
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class MatchSnapShotServiceImpl implements MatchSnapShotService {
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;

    @Override
    public List<OddsSnapShotVo> selectMatchSnapShot(Long matchId, Integer matchStatus) {
        List<OddsSnapShotVo> oddsSnapShotVos = standardSportMarketMapper.selectMatchSnapShot(matchId, matchStatus);
        return oddsSnapShotVos;
    }
}
