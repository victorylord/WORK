package com.panda.sport.rcs.pojo.dto.api;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 用户id
 * @Author : jordan
 * @Date : 2022-04-22 13:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@ApiModel(value = "用户id传输类")
public class UserIdDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long userId;

}