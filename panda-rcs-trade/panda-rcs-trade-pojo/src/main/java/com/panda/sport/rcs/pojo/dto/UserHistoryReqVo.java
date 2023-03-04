package com.panda.sport.rcs.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 标签变更记录 reqVO
 * </p>
 *
 * @author skyKong
 * @since 2021-03-11
 */
@ApiModel(value="UserHistoryReqVo reqVO", description="用户历史记录reqVO")
public class UserHistoryReqVo implements Serializable {

    @ApiModelProperty(value = "user")
    private String user;
    @ApiModelProperty(value = "商户数据权限：当前商户和商户下得数据由业务方提供")
    private List<String> merchantCodes=new ArrayList<>();

    @ApiModelProperty(value = "异常类型")
    private String type;

    @ApiModelProperty(value = "异常类型",hidden = true)
    private List<String> types=new ArrayList<>();

    @ApiModelProperty(value = "开始时间")
    @NotNull
    private Long startTime;

    @ApiModelProperty(value = "结束时间")
    @NotNull
    private Long endTime;

     @ApiModelProperty(value = "页数")
     @NotNull
     @Min(value = 1, message = "最小值为1")
     private Integer pageNum;

    @ApiModelProperty(value = "页大小")
    @NotNull
    @Min(value = 1, message = "最小值为1")
    private Integer pageSize;


    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getTypes() {
        return types;
    }
    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getMerchantCodes() {
        return merchantCodes;
    }
    public void setMerchantCodes(List<String> merchantCodes) {
        this.merchantCodes = merchantCodes;
    }

    public String getType() {return type;}
    public void setType(String type) {
        this.type = type;
    }


    public Long getStartTime() {
        return startTime;
    }
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getPageNum() {
        return pageNum;
    }
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {return pageSize;}
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }






}
