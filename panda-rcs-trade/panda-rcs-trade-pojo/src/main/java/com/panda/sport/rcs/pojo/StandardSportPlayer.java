package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 标准球员信息表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StandardSportPlayer extends RcsBaseEntity<StandardSportPlayer> {

    private static final long serialVersionUID = 1L;

    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * standard_sport_team.id。 首次入库时写入，其他时机可不用维护，后台可手动修改。
     */
    private Long standardTeamId;

    /**
     * 1：可见； 0：不可见
     */
    private Integer visible;

    /**
     * 取值: SR BC分别代表:SportRadar、FeedConstruc.详情见data_source
     */
    private String dataSourceCode;

    /**
     * 球员的原始id
     */
    private String thirdPlayerId;

    /**
     * 头像 url地址。
     */
    private String picture;

    /**
     * 球员中文名字.仅用用于数据库操作人员使用。
     */
    private String name;

    /**
     * 球员名称编码. 用于多语言
     */
    private Long nameCode;

    private String introduction;

    private String remark;

    private Long createTime;

    private Long modifyTime;


}
