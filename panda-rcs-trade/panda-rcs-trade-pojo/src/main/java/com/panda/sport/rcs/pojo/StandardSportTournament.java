package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName StandardSportTournament
 * @Description: TODO
 * @Author Vector
 * @Date 2019/10/8
 **/
@Data
public class StandardSportTournament extends RcsBaseEntity<StandardSportTournament> {
    private static final long serialVersionUID = 1L;
    /**
     * 表ID, 自增. id
     */
    private Long id;

    /**
     * 运动种类ID. 联赛所属体育种类id, 对应 sport.id
     */
    private Long sportId;

    /**
     * 第三方联赛id. 第三方联赛在 表 third_sport_tournament 中的id
     */
    private Long thirdTournamentId;

    /**
     * 所属标准区域 id.  对应  standard_sport_region.id
     */
    private Long regionId;

    /**
     * 联赛分级. 1: 一级联赛; 2:二级联赛; 3: 三级联赛; 以此类推; 0: 未分级
     */
    private Integer tournamentLevel;

    /**
     * 后台管理使用的联赛id.
     */
    private String tournamentManagerId;

    /**
     * 第三方联赛原始id.第三方提供的联赛的id
     */
    private String thirdTournamentSourceId;

    /**
     * 联赛名称编码. 联赛名称编码. 用于多语言
     */
    private Long nameCode;

    /**
     * 数据来源编码.取值: SR BC分别代表:SportRadar、FeedConstruc.详情见data_source
     */
    private String dataSourceCode;

    /**
     * 联赛 logo. 图标的url地址
     */
    private String logoUrl;

    /**
     * 联赛 logo. 缩略图的url地址
     */
    private String logoUrlThumb;

    /**
     * 关联数据源数量
     */
    private Integer relatedDataSourceCoderNum;

    /**
     * 关联数据源编码列表. 数据样例:SR,BC,188; SR,188; BC,188 (冗余字段,用于查询)
     */
    private String relatedDataSourceCoderList;

    /**
     * 简介.
     */
    private String introduction;

    /**
     * 备注.
     */
    private String remark;

    /**
     * 创建时间.
     */
    private Long createTime;

    /**
     * 修改时间.
     */
    private Long modifyTime;

    /**
     * 英文名称(冗余字段,用于排序)
     */
    private String nameSpell;

    /**
     * 中文简体(冗余字段,用于查询,修改是需要维护)
     */
    private String name;

    /**
     * @Description   赛事列表
     * @Param
     * @Author  toney
     * @Date  21:49 2020/3/18
     * @return
     **/
    @TableField(exist = false)
    private List<StandardMatchInfo> standardMatchInfoList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardSportTournament that = (StandardSportTournament) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    /**
     * 当为子联赛时取父联赛的id
     */
    private String fatherTournamentId;
    /**
     * 是否有对应的三方联赛（默认1）0：没有、1：有
     */
    private Integer hasRelation;
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