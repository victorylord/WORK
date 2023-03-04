package com.panda.sport.rcs.trade.vo.tourTemplate;

import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TemplateMenuListDto;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.vo.I18nItemVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  联赛查询类
 * @Date: 2020-05-10 21:20
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StandardSportTournamentListVo {
    private static final long serialVersionUID = 1L;

    /**
     * 联赛表ID, 自增. id
     */
    private Long id;

    /**
     * 是否热门1:是 0:否
     */
    private Integer isPopular;

    /**
     * 多语言
     */
    private List<I18nItemVo> languageCodeList;

    /**
     * 早盘所属模板
     */
    private TemplateMenuListDto template;

    /**
     * 滚球所属模板
     */
    private TemplateMenuListDto liveTemplate;

    public void setTemplate(RcsTournamentTemplate rcsTournamentTemplate) {
        if (rcsTournamentTemplate == null) {
            return;
        }
        TemplateMenuListDto vo = new TemplateMenuListDto();
        BeanCopyUtils.copyProperties(rcsTournamentTemplate, vo);
        this.template = vo;
    }

    public void setTemplate(TemplateMenuListDto template) {
        this.template = template;
    }

    public void setLiveTemplate(TemplateMenuListDto liveTemplate) {
        this.liveTemplate = liveTemplate;
    }

    public void setLiveTemplate(RcsTournamentTemplate rcsTournamentTemplate) {
        if (rcsTournamentTemplate == null) {
            return;
        }
        TemplateMenuListDto vo = new TemplateMenuListDto();
        BeanCopyUtils.copyProperties(rcsTournamentTemplate, vo);
        this.liveTemplate = vo;
    }

    /**
     * 早盘模板列表
     */
    private List<TemplateMenuListDto> menuList;
    /**
     * 滚球模板列表
     */
    private List<TemplateMenuListDto> liveMenuList;
    /**
     * 当为子联赛时取父联赛的id
     */
    private String fatherTournamentId;
    /**
     * 父联赛等级
     */
    private Integer fatherTournamentLevel;
    /**
     * 子级联赛数据
     */
    private List<ChildTournament> childTournament;
    @Data
    public static class ChildTournament {
        /**
         * 联赛id
         */
        private Long id;
        /**
         * 联赛级别
         */
        private Integer tournamentLevel;
        /**
         * 多语言
         */
        private List<I18nItemVo> languageCodeList;
    }
    /**
     * 综合球类接单延迟时间
     */
    private Integer orderDelayTime;
    /**
     * 目标咬度（即目标盈利率）
     */
    private BigDecimal targetProfitRate;
    /**
     * MTS赔接拒率变动范围
     */
    private BigDecimal MtsOddsChangeValue;
    /**
     * 赔率接拒变动开关(1.开 0.关) 默认关
     */
    private Integer oddsChangeStatus;
}
