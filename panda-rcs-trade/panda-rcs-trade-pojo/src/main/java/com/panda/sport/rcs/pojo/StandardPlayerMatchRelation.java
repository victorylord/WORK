package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 球员与赛事 关系表.

某个球员准备参与了某场比赛，则会在该表中增加一条记录且仅会增加一条记录。
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StandardPlayerMatchRelation extends RcsBaseEntity<StandardPlayerMatchRelation> {

    private static final long serialVersionUID = 1L;

    /**
     * 数据库维护，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对应   standard_sport_player.id
     */
    private Long standardPlayerId;

    /**
     * 对应   standard_match_info.id
     */
    private Long standardMatchId;

    /**
     * 标准球队id。
     */
    private Long standardTeamId;

    /**
     * 有效截止时间。UTC时间。 当该字段的值大于当前时间时，当前比赛相关业务在继续。 当前字段小于当前时间时，本场比赛的业务在该系统中终止。
     */
    private Long effectEndTime;

    /**
     * 取值: SR BC分别代表:SportRadar、FeedConstruc.详情见data_source
     */
    private String dataSourceCode;

    private String remark;

    private Long createTime;

    private Long modifyTime;


}
