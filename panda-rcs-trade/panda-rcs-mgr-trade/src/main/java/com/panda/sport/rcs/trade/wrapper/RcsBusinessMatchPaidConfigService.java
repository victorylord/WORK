package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsBusinessMatchPaidConfig;
import com.panda.sport.rcs.vo.MatchPaidVo;
import com.panda.sport.rcs.vo.TournamentVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 赛事最大赔付配置 服务类
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
public interface RcsBusinessMatchPaidConfigService extends IService<RcsBusinessMatchPaidConfig> {

    List<TournamentVo> selectTournaments(TournamentVo tournamentVo);
}
