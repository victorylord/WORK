package com.panda.sport.rcs.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.pojo.RcsTradingAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-11-07 17:24
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingAssignmentDataVo {
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
    private Set<Long> playCollectionIdList=new HashSet<>();
    /**
     * 0不是赛事负责人  1是赛事负责人
     */
    private Integer IsResponsible;
    /**
     * 名字字符串  :隔开
     */
    private transient String nameIds;
    /**
     * 体育种类
     */
    private transient Long  sportId;

    public TradingAssignmentDataVo(RcsTradingAssignment rcsTradingAssignment) {
        this.id=rcsTradingAssignment.getId();
        this.matchId=rcsTradingAssignment.getMatchId();
        this.matchType=rcsTradingAssignment.getMatchType();
        this.userId=rcsTradingAssignment.getUserId();
        this.playCollectionId=rcsTradingAssignment.getPlayCollectionId();
        this.optionUserId=rcsTradingAssignment.getOptionUserId();
        this.status=rcsTradingAssignment.getStatus();
    }
}
