package com.panda.sport.rcs.trade.wrapper.tourTemplate;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfigAutoChange;


public interface RcsTournamentTemplateAcceptConfigAutoChangeService extends IService<RcsTournamentTemplateAcceptConfigAutoChange> {

    /**
     * 修改开关
     */
    void updateTemplateAcceptConfigAutoChange(RcsTournamentTemplateAcceptConfigAutoChange config);

    RcsTournamentTemplateAcceptConfigAutoChange queryTemplateAcceptConfigAutoChange(RcsTournamentTemplateAcceptConfigAutoChange configAutoChange);
}
