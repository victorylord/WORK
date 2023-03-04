package com.panda.sport.rcs.mapper.tourTemplate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateJumpConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.tourTemplate.TournamentTemplatePlayMargainOddsResVo;
import com.panda.sport.rcs.pojo.tourTemplate.TournamentTemplatePlayMargainResVo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RcsTournamentTemplateJumpConfigMapper extends BaseMapper<RcsTournamentTemplateJumpConfig> {

    int insertBatch(RcsTournamentTemplateJumpConfig param);
    List<TournamentTemplatePlayMargainResVo> selectTournamentSpecialOddsIntervalInitData();
    List<StandardSportTournament> selectMTSOddsChangeValue();
    List<TournamentTemplatePlayMargainOddsResVo> selectTournamentOddsChangeValue();
}
