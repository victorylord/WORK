package com.panda.sport.rcs.trade.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-13 16:21
 **/
@Data
public class BasketballMatrixDataVo {
    /**
     * 赛事Id
     */
    private Integer matchId;
    /**
     * 所属时段  theirTimeType
     */
    private Integer theirTime;
    /**
     * 玩法Id
     */
    private List<Integer> playIdList;
    /**
     * 商户id
     */
    private List<Long>  merchantIdList;
    /**
     * 数值单位  1 100 1000 10000
     */
    private Integer unit;
    /**
     * 矩阵列数
     */
    private Integer column;
    /**
     * 分差中值
     */
    private Integer differential;
    /**
     * 总分总值
     */
    private Integer totalScore;
    /**
     * 是否结算  2未结算  1已结算 不填全部
     */
    private List<Integer> settlement;
    /**
     * 1赛前 2滚球 不填全部
     */
    private List<Integer> matchType;
    /**
     * 返回的数据
     */
    List<MatrixDataVo> newTotalHashmap=new ArrayList();
    /**
     *返回的数据
     */
    List<MatrixDataVo> newHalfTotalHashmap=new ArrayList<>();

    /**
     * 当前总分
     */
    private Integer currentTotalScore;
    /**
     * 当前分差
     */
    private Integer currentDifferential;
}
