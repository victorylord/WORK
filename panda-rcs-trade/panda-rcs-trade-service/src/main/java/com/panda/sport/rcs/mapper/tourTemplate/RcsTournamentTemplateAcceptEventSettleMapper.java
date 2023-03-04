package com.panda.sport.rcs.mapper.tourTemplate;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEvent;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEventSettle;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @Description   联赛模板结算事件从表
 * @Param 
 * @Author  carver
 * @Date  20:02 2021/10/09
 * @return 
 **/
@Repository
public interface RcsTournamentTemplateAcceptEventSettleMapper extends BaseMapper<RcsTournamentTemplateAcceptEventSettle> {

    /**
     * @Description   //更新事件状态
     * @Param [config]
     * @Author  carver
     * @Date  16:12 2021/10/17
     * @return void
     **/
    void updateMatchEventConfigSettle(@org.apache.ibatis.annotations.Param("list") List<RcsTournamentTemplateAcceptEventSettle> config);
}