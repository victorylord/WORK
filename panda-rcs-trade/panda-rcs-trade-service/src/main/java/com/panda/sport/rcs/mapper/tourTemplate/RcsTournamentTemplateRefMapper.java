package com.panda.sport.rcs.mapper.tourTemplate;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TournamentTemplateRefDto;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateRef;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RcsTournamentTemplateRefMapper extends BaseMapper<RcsTournamentTemplateRef> {

    int insertOrUpdate(RcsTournamentTemplateRef rcsTournamentTemplateRef);

    List<TournamentTemplateRefDto> selectTemplateByTournamentId(List<Long> tournamentIds);
}