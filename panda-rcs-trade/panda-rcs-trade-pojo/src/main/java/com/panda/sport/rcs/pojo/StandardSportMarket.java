package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 足球赛事盘口表. 使用盘口关联的功能存在以下假设: 同一个盘口的显示值不可变更, 如果变更需要删除2个盘口之间的关联关系. .
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
public class StandardSportMarket extends RcsBaseEntity<StandardSportMarket> {

    private static final long serialVersionUID = 1L;

    /**
     * 数据库id, 自增
     */
//      @TableId(value = "id", type = IdType.AUTO)  禁用自增长，采用标准盘口ID作为主键
    private Long id;

    /**
     * 运动种类id.  对应表 sport.id
     */
    private Long sportId;

    /**
     * 所属联赛ID    standard_sport_tournament.id
     */
    private Long standardTournamentId;

    /**
     * 标准比赛ID   standard_match_info.id
     */
    private Long standardMatchInfoId;

    /**
     * 标准玩法id   standard_sport_market_category.id
     */
    private Long marketCategoryId;

    /**
     * 盘口类型. 属于赛前盘或者滚球盘. 1: 赛前盘; 0: 滚球盘.
     */
    private Integer marketType;

    /**
     * 该盘口具体显示的值. 例如: 大小球中, 大小界限是:  3.5
     */
    private String oddsValue;

    /**
     * 盘口名称,V1.2统一命名规则.
     */
    private String oddsName;

    /**
     * 盘口名称编码
     */
    private Long nameCode;

    /**
     * 排序类型
     */
    private String orderType;

    /**
     * 附加字段1
     */
    private String addition1;

    /**
     * 附加字段2
     */
    private String addition2;

    /**
     * 附加字段3
     */
    private String addition3;

    /**
     * 附加字段4
     */
    private String addition4;

    /**
     * 附加字段5
     */
    private String addition5;

    /**
     * 取值:  SR BC分别代表: SportRadar、FeedConstruc. 详情见data_source
     */
    private String dataSourceCode;

    /**
     * 盘口状态0-5. 0:active, 1:suspended, 2:deactivated, 3:settled, 4:cancelled, 5:handedOver
     */
    private Integer status;

    private Integer thirdMarketSourceStatus;

    private Integer paStatus;

    @TableField("end_ed_status")
    private Integer endEdStatus;

    private Integer placeNumStatus;

    /**
     * 盘口阶段id. 对应 sport_market_scope.id
     */
    private String scopeId;

    /**
     * 1 需要;  0 不需要
     */
    private Integer managerConfirmPrize;

    /**
     * 盘口差
     */
    private BigDecimal marketHeadGap;

    /**
     * 该字段用于做风控时, 需要替换成风控服务商提供的盘口id.  如果数据源发生切换, 当前字段需要更新.
     */
    private String thirdMarketSourceId;

    /**
     * 盘口名称多语言数组json串
     */
    private String i18nNames;

    private String remark;

    /**
     * 扩展参数：用于上游需通过融合向下游透传的参数，融合不做任何处理及存储
     */
    private String extraInfo;

    private Long createTime;

    private Long modifyTime;

    /**
     * 盘口级别，数字越小优先级越高
     */
    private Integer oddsMetric;
    /**
     * 盘口位置
     */
    @TableField(exist = false)
    private Integer placeNum;
    /**
     * @Description //赔率列表
     * @Param
     * @Author Sean
     * @Date 15:17 2020/10/6
     * @return
     **/
    @TableField(exist = false)
    private List<StandardSportMarketOdds> marketOddsList;

    /**
     * 盘口ID
     */
    @TableField(exist = false)
    private String marketIdStr;

    /**
     * 主盘标志，1-主盘
     */
    @TableField(exist = false)
    private Integer mainFlag;
    /**
     * 子玩法ID
     */
    private Long childMarketCategoryId;
    /**
     * 盘口来源 0：数据商 1：融合构建
     */
    private Integer marketSource;

    public String getMarketIdStr() {
        if (this.id != null) {
            return this.id.toString();
        }
        return null;
    }

    public Double getMarketHeadGap2() {
        if (this.marketHeadGap != null) {
            return this.marketHeadGap.doubleValue();
        }
        return null;
    }
}
