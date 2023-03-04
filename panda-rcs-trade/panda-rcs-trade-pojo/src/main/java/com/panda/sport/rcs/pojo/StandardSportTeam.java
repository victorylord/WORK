package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Objects;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 标准球队信息表.
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
/*@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)*/
public class StandardSportTeam extends RcsBaseEntity<StandardSportTeam> {

    private static final long serialVersionUID = 1L;

    /**
     * id. id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 体育种类id. 体育种类id
     */
    private Long sportId;

    /**
     * 第三方球队id.    third_sport_team.id
     */
    private Long thirdTeamId;

    /**
     * 球队区域ID.   standard_sport_region.id
     */
    private Long regionId;

    /**
     * 关联数据源数量
     */
    private Integer relatedDataSourceCoderNum;

    /**
     * 关联数据源编码列表.  数据样例: SR,BC,188; SR,188; BC,188
     */
    private String relatedDataSourceCoderList;

    /**
     * 球队 logo. 图标的url地址
     */
    private String logoUrl;

    /**
     * 球队 logo缩略图的url地址
     */
    private String logoUrlThumb;

    /**
     * 球队管理id.  该id 用于后台管理. 
     */
    private String teamManageId;

    /**
     * 球队名称编码.  用于多语言
     */
    private Long nameCode;

    /**
     * 主教练.主教练名称
     */
    private String coach;

    /**
     * 主场.比如:所在地 和 名称 
     */
    private String statium;

    /**
     * 球队介绍. 默认是空
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
     * 更新时间. 
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
     * 数据来源编码.取值: SR BC分别代表:SportRadar、FeedConstruc.详情见data_source
     */
    private String dataSourceCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardSportTeam that = (StandardSportTeam) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
