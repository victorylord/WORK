package com.panda.sport.rcs.pojo;

import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单单号
     */
    @TableId(value = "order_no")
    private String orderNo;


    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 订单状态(0:待处理,1:已处理,2:取消交易)
     */
    private Integer orderStatus;

    /**
     * 注单项数量
     */
    private Long productCount;

    /**
     * 串关类型(0:单注(默认) 1:双式投注,例如1/2  2:三式投注,例如1/2/3   3:N串1,例如4串1   4:N串F,例如5串26 )
     */
    private Integer seriesType;

    /**
     * 注单总价
     */
    private Long productAmountTotal;

    /**
     * 实际付款金额
     */
    private Long orderAmountTotal;

    /**
     * 1:手机，2：PC
     */
    private Integer deviceType;

    /**
     * ip地址
     */
    private String ip;


    /**
     * 商户id
     */
    private Long tenantId;


    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 币种编码
     */
    private String currencyCode;

    /**
     * 详情
     */
    private Integer infoStatus = 0;

    @TableField(exist = false)
    private Integer orginInfoStatus = 0;

    private String ipArea;

    private String reason;
    /**
     * @Description   订单列表
     * @Param 
     * @Author  toney
     * @Date  15:32 2020/1/31
     * @return 
     **/
    @TableField(exist = false)
    private List<TOrderDetail> orderDetailList;

    /**
     * @Description   订单扩展
     * @Param 
     * @Author  toney
     * @Date  16:05 2020/1/31
     * @return 
     **/
    @TableField(exist = false)
    private List<TOrderDetailExt> orderDetailExtList;

}
