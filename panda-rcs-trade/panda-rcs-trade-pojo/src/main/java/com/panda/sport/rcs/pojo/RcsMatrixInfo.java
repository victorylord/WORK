package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 矩阵信息实体类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
public class RcsMatrixInfo extends RcsBaseEntity<RcsMatrixInfo> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 赛事编号
     */
    private Long matchId;

    /**
     * 矩阵类型（ 1.全场比分矩阵 2.上半场比分矩阵 3.下半场比分矩阵 4.全场角球矩阵 5.半场角球矩阵 6.全场加时矩阵 7.半场加时矩阵）
     */
    private Integer matrixType;

    /**
     * 玩法类型（ 全场： 1.波胆 2.双重机会 3.其他次要玩法 4.主要玩法 角球： 5.角球独赢 6.角球让球 7.角球大小 8.其他角球）
     */
    private Integer playType;

    /**
     * 商户类型（1.现金网 2.信用网）
     */
    private Integer businessType;

    /**
     * 赛事类型（1.早盘 2.滚球）
     */
    private Integer matchType;

    /**
     * 矩阵数据二维数组
     */
    private String recVal;
}
