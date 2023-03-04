package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  联赛模板
 * @Date: 2020-05-12 16:40
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsTournamentTemplate implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    /**
     * id
     */
    private Long id;

    /**
     * sportId
     */
    private Integer sportId;

    /**
     * 1：等级模板  2：专用模板   3：赛事模板
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
     * 比分源1:SR  2:UOF
     */
    private Integer scoreSource;

    /**
     * 赛事模板专用（模板复制来源名称）
     */
    private String templateName;

    /**
     *  生成赛事模板对应复制的模板id
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
     * 赛事提前结算开关 0:关 1:开
     */
    private Integer matchPreStatus;
    /**
     * 赔率变动接拒开关（0.关 1.开）
     */
    private Integer oddsChangeStatus;

    /**
     * 是否出涨自动封盘（0.关 1.开）
     */
    private Integer ifWarnSuspended;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date createTime;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date updateTime;

    /**
     * 	警示值
     */
    private BigDecimal cautionValue;

    /**
     * 	百家赔各参考网值
     */
    private String baijiaConfigValue;
    /**
     * ao数据源配置信息
     */
    private String aoConfigValue;

    /**
     * mts接距配置信息
     */
    private String mtsConfigValue;

    /**
<<<<<<< HEAD
     * 接距开关（0.关 1.开）默认0
     */
    private Integer distanceSwitch;
    /**
     * 预约投注开关 0:关 1:开
     */
    private Integer pendingOrderStatus;
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
    /*
     * 提交结算开关数据源配置
     * {"SR":1,"AO":0}  1表示选中
     */
    private String earlySettStr;
}
