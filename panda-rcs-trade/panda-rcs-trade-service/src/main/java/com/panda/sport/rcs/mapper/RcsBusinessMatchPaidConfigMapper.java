package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsBusinessMatchPaidConfig;
import com.panda.sport.rcs.vo.MatchPaidVo;
import com.panda.sport.rcs.vo.TournamentVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 赛事最大赔付配置 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
public interface RcsBusinessMatchPaidConfigMapper extends BaseMapper<RcsBusinessMatchPaidConfig> {

    void updateRcsBusinessMatchPaids(@Param("rcsBusinessMatchPaids")List<RcsBusinessMatchPaidConfig> rcsBusinessMatchPaids);

    List<MatchPaidVo> getMatchPaidListView();

    List<TournamentVo> selectTournaments(@Param("tournamentVo") TournamentVo tournamentVo);

    List<RcsBusinessMatchPaidConfig> selectList(@Param("businessId")Long businessId,@Param("sportId") Long sportId );
}
