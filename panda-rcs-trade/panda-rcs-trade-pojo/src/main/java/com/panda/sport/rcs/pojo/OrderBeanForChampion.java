package com.panda.sport.rcs.pojo;

import com.panda.sport.data.rcs.dto.ExtendBean;
import com.panda.sport.data.rcs.dto.OrderItem;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-03
 */
public class OrderBeanForChampion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String traceId;

    /**
     * 运动种类编号
     */
    private Integer sportId;

    /**
     * 订单单号
     */
    private String orderNo;
    /**
     * 用户ID
     */
    private String uid;
    
    
    /**
     * 是否自动接受赔率变化 1：自动接收更好的赔率 2：自动接受任何赔率变动 3：不自动接受赔率变动
     */
    private Integer acceptOdds;

    /**
     * 订单状态(0:待处理,1:已处理,2:取消交易)
     */
    private Integer orderStatus = 0;

    /**
     * 订单状态(0:未处理,1:已处理)
     */
    private Integer handleStatus = 0;

    /**
     * 注单项数量
     */
    private Long productCount;

    /**
     * 串关类型(1：单关(默认)  )
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
     * 来源渠道编码
     * MTS的订单，还是PA
     */
    private Integer channelCode;

    /**
     * 商户id
     */
    private Long tenantId;
    /**
     * 商户名称(新加)
     */
    private String tenantName;


    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * MTS接受期间会有赔率变化，需要做更新
     */
    private List<Map<String, Object>> oddsChangeList;

    /*
     * @Description   用户标签(新加)
     * @Param
     * @Author  toney
     * @Date  16:03 2019/11/4
     * @return
     **/
    private String userFlag;

    /**
     * 用户标签id
     */
    private Integer userFlagId;
    
    /**
     * 一级标签 用户等级
     */
    private int userTagLevel ;
    /**
     * @Description   用于前端展示用，从t_user表取
     * @Param 
     * @Author  toney
     * @Date  21:30 2020/6/3
     * @return 
     **/
    private String username;

    /**
     * 详情
     */
    private Integer infoStatus = 0;

    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 币种编码
     */
    private String currencyCode;

    /**
     * 币种名称(新加)
     */
    private String currencyName;

    /**
     * ip用户区域
     */
    private String ipArea;
    
    /**
     * VIP等级： 0 非VIP，1 VIP用户
     */
    private Integer vipLevel;

    /**
     * 校验结果  1：成功  2：失败
     */
    private Integer validateResult=2;
    /**
     * 注单集合
     */
    private List<OrderItem> items;

    private String reason;
    
    private Boolean isUpdateOdds;
    
    /**
     * 二级标签
     */
    private String secondaryTag ;
    
    //*******************以下字段计算使用**************************//
    transient private ExtendBean extendBean;

    public Integer getHandleStatus() {
        return handleStatus;
    }

    public int getUserTagLevel() {
		return userTagLevel;
	}

	public void setUserTagLevel(int userTagLevel) {
		this.userTagLevel = userTagLevel;
	}

	public void setHandleStatus(Integer handleStatus) {
        this.handleStatus = handleStatus;
    }

    public Integer getSportId() {
        return sportId;
    }

    public Boolean getIsUpdateOdds() {
		return isUpdateOdds;
	}

	public void setIsUpdateOdds(Boolean isUpdateOdds) {
		this.isUpdateOdds = isUpdateOdds;
	}

	public String getSecondaryTag() {
		return secondaryTag;
	}

	public void setSecondaryTag(String secondaryTag) {
		this.secondaryTag = secondaryTag;
	}

	public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }

    public List<Map<String, Object>> getOddsChangeList() {
		return oddsChangeList;
	}

	public void setOddsChangeList(List<Map<String, Object>> oddsChangeList) {
		this.oddsChangeList = oddsChangeList;
	}

	public Integer getChannelCode() {
		return channelCode;
	}

	public Integer getAcceptOdds() {
		return acceptOdds;
	}

	public void setAcceptOdds(Integer acceptOdds) {
		this.acceptOdds = acceptOdds;
	}

	public void setChannelCode(Integer channelCode) {
		this.channelCode = channelCode;
	}

	public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Long getProductCount() {
        return productCount;
    }

    public Integer getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(Integer vipLevel) {
		this.vipLevel = vipLevel;
	}

	public void setProductCount(Long productCount) {
        this.productCount = productCount;
    }

    public Integer getInfoStatus() {
        return infoStatus;
    }

    public void setInfoStatus(Integer infoStatus) {
        this.infoStatus = infoStatus;
    }

    public Integer getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(Integer seriesType) {
        this.seriesType = seriesType;
    }

    public Long getProductAmountTotal() {
        return productAmountTotal;
    }

    public void setProductAmountTotal(Long productAmountTotal) {
        this.productAmountTotal = productAmountTotal;
    }

    public Long getOrderAmountTotal() {
        return orderAmountTotal;
    }

    public void setOrderAmountTotal(Long orderAmountTotal) {
        this.orderAmountTotal = orderAmountTotal;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }


    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getUserFlag() {
        return userFlag;
    }

    public void setUserFlag(String userFlag) {
        this.userFlag = userFlag;
    }

    public Integer getUserFlagId() {
        return userFlagId;
    }

    public void setUserFlagId(Integer userFlagId) {
        this.userFlagId = userFlagId;
    }

    public String getUsername() {
        if(StringUtils.isEmpty(username)){
            return "";
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }


    public String getIpArea() {
        return ipArea;
    }

    public void setIpArea(String ipArea) {
        this.ipArea = ipArea;
    }

    public Integer getValidateResult() {
        return validateResult;
    }

    public void setValidateResult(Integer validateResult) {
        this.validateResult = validateResult;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public ExtendBean getExtendBean() {
        return extendBean;
    }

    public void setExtendBean(ExtendBean extendBean) {
        this.extendBean = extendBean;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

}
