package com.panda.sport.rcs.trade.vo.tourTemplate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  联赛模板返回类
 * @Date: 2020-05-12 21:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplateListVo {
    /**
     * 赛种
     */
    private Integer sportId;

    /**
     * 1：级别  2：联赛id   3：赛事id
     */
    private Integer type;

    /**
     * 根据type设置（1：设值联赛等级  2：设值联赛id   3：设值赛事id）
     */
    private Long typeVal;

    /**
     * 联赛列表
     */
    IPage<StandardSportTournamentListVo> page;
}
