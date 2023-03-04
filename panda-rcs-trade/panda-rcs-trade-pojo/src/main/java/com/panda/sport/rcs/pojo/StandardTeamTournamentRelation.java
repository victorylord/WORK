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
 * 球队联赛关系表  
一支球队参加某联赛,该表中存且仅存放一条信息。
如果 意大利队,参加 200
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StandardTeamTournamentRelation extends RcsBaseEntity<StandardTeamTournamentRelation> {

    private static final long serialVersionUID = 1L;

    /**
     * 数据库维护该id，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标准球队id
     */
    private Long standardTeamId;

    /**
     * 标准联赛id
     */
    private Long standardTournamentId;

    /**
     * 取值为正整数； 1为冠军；2为亚军；3为季军；4第四名；5第五名；
     */
    private Integer tournamentOrder;

    /**
     * 联赛年份，用于区别不同时间的联赛
     */
    private Integer tournamentYear;

    /**
     * 取值: SR BC分别代表:SportRadar、FeedConstruc.详情见data_source
     */
    private String dataSourceCode;

    /**
     * 联赛名称,便于维护查询.仅用用于数据库操作人员使用。
     */
    private String tournamentName;

    /**
     * 球队名称,便于维护查询.仅用用于数据库操作人员使用。
     */
    private String teamName;

    private String remark;

    private Long createTime;

    private Long modifyTime;


}
