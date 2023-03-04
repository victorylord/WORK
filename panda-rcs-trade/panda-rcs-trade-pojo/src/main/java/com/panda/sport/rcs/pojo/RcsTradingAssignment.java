package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @program: xindaima
 * @description: 指派风控集数据
 * @author: kimi
 * @create: 2020-11-07 11:37
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsTradingAssignment {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 赛事Id
     */
    private Long matchId;
    /**
     * 赛事类型 0早盘 1滚球
     */
    private Integer matchType;
    /**
     * 操盘手Id
     */
    private String userId;
    /**
     * 玩法集Id
     */
    private Long playCollectionId;
    /**
     * 操作用户Id
     */
    private String optionUserId;
    /**
     * 状态1有效 2或者其他无效
     */
    private Integer status;
    /**
     * 选中的玩法集
     */
    @TableField(exist = false)
    private List<Long>  playCollectionIdList;
}
