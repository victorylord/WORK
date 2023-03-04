package com.panda.sport.rcs.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 保存标准赛事与球队之间的所属关系。 
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StandardMatchTeamRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标准球队id
     */
    private Long standardTeamId;

    /**
     * 球队名称（中文）
     */
    private String teamName;

    /**
     * 标准比赛id
     */
    private Long standardMatchId;

    /**
     * 比赛中的作用。足球：主客队或者其他.home:主场队;away:客场队
     */
    private String matchPosition;

    /**
     * 显示顺序。 默认不使用
     */
    private Integer displayOrder;

    /**
     * 参加该比赛时，球队的国际化名称，存档数据
     */
    private String teamNameRecord;

    private String remark;

    private Long createTime;

    private Long modifyTime;


}
