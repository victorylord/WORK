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
public class RcsMatrixInfoReqVo extends RcsMatrixInfo implements Serializable {

    /**
     * 单位 1 10 100 1000
     */
    private Integer unit;
    /**
     * 矩阵大小 5*5 / 6*6
     */
    private Integer size;

    /**
     * 玩法类型（ 全场： 1.波胆 2.双重机会 3.其他次要玩法 4.主要玩法 角球： 5.角球独赢 6.角球让球 7.角球大小 8.其他角球）
     */
    private Integer[] playTypes;

    /**
     * 商户类型（1.现金网 2.信用网）
     */
    private Integer[] businessTypes;

    /**
     * 赛事类型（1.早盘 2.滚球）
     */
    private Integer[] matchTypes;

    /**
     * 提前结算 0、非提前结算 1、提前结算
     */
    private Integer earlySettlementType;
}
