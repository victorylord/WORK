package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.MatchPeriod;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.List;

/**
 * @ClassName MatchPeriodService
 * @Description: TODO
 * @Author Vector
 * @Date 2019/11/19
 **/
public interface MatchPeriodService extends IService<MatchPeriod> {
    /**
     * 查询阶段比分
     * @param macthId
     * @param period
     * @return
     */
    MatchPeriod getOne(Long macthId, Integer period);

    String  selectRecentScoreTeam(Long macthId, String score);

}
