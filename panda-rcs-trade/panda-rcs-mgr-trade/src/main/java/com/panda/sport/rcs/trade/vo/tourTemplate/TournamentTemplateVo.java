package com.panda.sport.rcs.trade.vo.tourTemplate;

import com.google.common.collect.Lists;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEvent;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateEvent;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  联赛模板
 * @Date: 2020-05-12 19:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplateVo {
    /**
     * 主键id
     */
    private Long id;
    /**
     * sportId
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
     * 1：早盘；0：滚球
     */
    private Integer matchType;
    /**
     * 赔率源
     */
    private String dataSourceCode;
    /**
     * 商户单场赔付限额
     */
    private Long businesMatchPayVal;

    /**
     * 用户单场赔付限额
     */
    private Long userMatchPayVal;
    /**
     * 商户单场预约赔付限额
     */
    private Long businesPendingOrderPayVal;
    /**
     * 用户单场预约赔付限额
     */
    private Long userPendingOrderPayVal;
    /**
     * 用户预约中笔数
     */
    private Integer userPendingOrderCount;
    /**
     * 预约投注速率
     */
    private Integer pendingOrderRate;
    /**
     * 比分源1:SR  2:UOF
     */
    private Integer scoreSource;

    /**
     * 赛事模板专用（模板复制来源名称）
     */
    private String templateName;
    /**
     * 玩法总数量
     */
    private Integer totalNum;
    /**
     * 已开售玩法总数量
     */
    private Integer sellNum;
    /**
     * 生成赛事模板对应复制的模板id
     */
    private Long copyTemplateId;
    /**
     * 常规接单等待时间
     */
    private Integer normalWaitTime;
    /**
     * 暂停接单等待时间
     */
    private Integer pauseWaitTime;
    /**
     * 结算/审核事件
     */
    private List<TournamentTemplateEventVo> templateEventList;
    /**
     * 玩法集
     */
    private List<TournamentTemplateCategorySetVo> categorySetList;
    /**
     * 玩法赔率源设置
     */
    private Map categoryOddsConfig;
    /**
     * 主键id
     */
    private Long fatherTemplateId;
    /**
     * 赛事提前结算开关 0:关 1:开
     */
    private Integer matchPreStatus;
    /**
     * 赔率变动接拒开关（0.关 1.开）
     */
    private Integer oddsChangeStatus;
    /**
     * 预约投注开关 0:关 1:开
     */
    private Integer pendingOrderStatus;
    /**
     * 	警示值
     */
    private BigDecimal cautionValue;

    /**
     * 百家赔各参考网值
     */
    private String baijiaConfigValue;

    /**
     * 是否出涨自动封盘（0.关 1.开）
     */
    private Integer ifWarnSuspended;

    /**
     * ao数据源各参考值
     */
    private String aoConfigValue;

    /**
     * mts配置信息
     */
    private String mtsConfigValue;
    /**
     * 接距开关（0.关 1.开）默认0
     */
    private Integer distanceSwitch;

    /**
     * 提交结算开关数据源配置
     * {"SR":1,"AO":0}  1表示选中
     */
    private String earlySettStr;
}
