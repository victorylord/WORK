package com.panda.sport.rcs.mapper.tourTemplate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentPlayMarginTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author :  carver
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.mapper.tourTemplate
 * @Description :  TODO
 * @Date: 2020-08-05 20:11
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsTournamentPlayMarginTemplateMapper extends BaseMapper<RcsTournamentPlayMarginTemplate> {
    /**
     * @Description: 根据联赛等级获取玩法模板数据
     * @Author  carver
     * @Date  2020/10/26 10:33
     * @param param:
     * @return: java.util.List<com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentPlayMarginTemplate>
     **/
    List<RcsTournamentPlayMarginTemplate> queryPlayTemplateInitData(RcsTournamentPlayMarginTemplate param);
}
