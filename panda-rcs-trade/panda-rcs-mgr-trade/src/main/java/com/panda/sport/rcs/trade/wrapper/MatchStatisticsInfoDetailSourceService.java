package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetailSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchStatisticsInfoDetailSourceService extends IService<MatchStatisticsInfoDetailSource> {
    List<MatchStatisticsInfoDetailSource> getThirdMatchScoreList(Long matchId, String dataSourceCode);

}
