package com.panda.sport.rcs.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.tools.ant.types.resources.selectors.Exists;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 * 投注单详细信息表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TOrderDetail extends RcsBaseEntity<TOrderDetail> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 注单编号
     */
    private String betNo;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 运动种类编号
     */
    private Integer sportId;

    /**
     * 运动种类名称
     */
    private String sportName;

    /**
     * 玩法ID
     */
    private Integer playId;

    /**
     * 玩法名称
     */
    private String playName;

    /**
     * 赛事编号
     */
    private Long matchId;


    /**
     * 下注时间
     */
    private Long betTime;

    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;

    /**
     * 盘口类型(OU:欧盘 HK:香港盘 US:美式盘 ID:印尼盘 MY:马来盘 GB:英式盘）
     */
    private String marketType;

    /**
     * 盘口Id
     */
    private Long marketId;
    /**
     * 盘口值
     */
    private String marketValue;

    /**
     * 盘口值，初始
     */
    private String marketValueNew;


    /**
     * 对阵信息
     */
    private String matchInfo;

    /**
     * 注单金额，指的是下注本金2位小数，投注时x100
     */
    private Long betAmount;

    /**
     * 除了100
     * @return
     */
    public BigDecimal getBetAmount1(){
        if(betAmount == null){
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(betAmount).divide(BigDecimal.valueOf(100));
    }

    /**
     * 注单赔率,固定2位小数 (结算时用到)
     */
    private Double oddsValue;

    /**
     * 变化前的赔率
     */
    private String oddFinally;


    /**
     * 最高可赢金额
     */
    private Double maxWinAmount;

    /**
     * 返还用户金额
     * @return
     */
    public BigDecimal getPaidAmount(){
        if(betAmount == null || oddsValue==null){
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(getBetAmount()).multiply(new BigDecimal(Double.toString(oddsValue)));
    }

    /**
     * 获取期望值
     * 下注金额 - 最大赔付金额
     * @return
     */
    public BigDecimal getProfitValueAmount(){
        return getBetAmount1().subtract(getPaidAmount());
    }



    /**
     * 基准比分(下注时已产生的比分)
     */
    private String scoreBenchmark;

    /**
     * 投注类型ID(对应上游的投注项ID),传给风控的
     */
    private Long playOptionsId;

    /**
     * 投注类型(投注时下注的玩法选项)，规则引擎用
     */
    private String playOptions;

    /**
     * 投注类型范围（所有投注的可能性-范围玩法时有值）
     */
    private String playOptionsRange;

    /**
     * 赛事阶段id
     */
    private Long matchProcessId;

    /**
     * 串关类型(0:单注(默认) 1:双式投注,例如1/2  2:三式投注,例如1/2/3   3:N串1,例如4串1   4:N串F,例如5串26 )
     */
    private Integer seriesType;

    /**
     * 创建时间
     */
    private Long createTime;


    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 联赛id
     */
    private Long tournamentId;

    /**
     * 是否需要和风控赛果进行对比
     */
    //@TableField(exist = false)
    //private String isResult;

    /**
     * 当前订单矩阵类型
     */
    private Integer recType;

    /**
     * 订单对应推算的矩阵数据
     */
    private String recVal;

    /**
     * @Description 计算矩阵是否需要当前比分
     * 0 不需要
     * 1 需要
     * @Param
     * @Author max
     * @Date 13:38 2019/10/12
     * @return
     **/
    private int isRelationScore;

    /**
     * 校验结果  1：成功  2：失败
     */
    private Integer validateResult;

    /**
     * 注单状态：1-已结算；2-未结算
     */
    private Integer isSettlement;

    /**
     * 订单风控验证渠道  1 : 内部风控  2 : mts
     */
    private Integer riskChannel;

    /**

     * 投注项名称(新加)
     */
    private String playOptionsName;
    /**
     * 接单模式 0 自动 1 手动
     */
    @TableField(exist = false)
    private Integer tradeType;
    /**
     * 订单状态(0:待处理,1:已处理,2:取消交易)
     */
    private Integer orderStatus = 0;

    /**
     * @Description 订单扫描状态 0 未处理 1 已处理
     * @Param
     * @Author toney
     * @Date 11:40 2020/1/31
     * @return
     **/
    @TableField(exist = false)
    private Integer handleStatus;

    public Double getHandleAfterOddsValue() {
        if(  oddsValue==null){
            return 0D;
        }
        return new BigDecimal(oddsValue + "").divide(new BigDecimal("100000")).setScale(2, RoundingMode.DOWN).doubleValue();
    }

    /**
     * 当前用户的标签 货量百分比
     */
    private BigDecimal volumePercentage;
}
