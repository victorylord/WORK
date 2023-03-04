package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description   和业务同步用户基础信息
 * @Param
 * @Author  toney
 * @Date  17:02 2020/6/3
 * @return
 **/
@Data
public class TUser extends RcsBaseEntity<TUser> {
    /**
     * 版本号
     */
    private static final long serialVersionUID = -5591067906256831859L;
    /**
     * 用户id
     */
    @TableId(value = "uid")
    private Long uid;
    /**
     * 用户状态 0启用 1禁用
     */
    private Integer disabled;

    /**
     * 用户名
     */
    private String username;

    public String getUsername(){
        return username == null ? "":username;
    }

    /**
     * 用户登录密码
     */
    private String password;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 是否测试用户 0 否 1 是
     */
    private Integer isTest;

    /**
     * 用户等级
     */
    private Integer userLevel;
    /**
     * 用户ip地址
     */
    private String ipAddress;
    /**
     * 1人民币,2美元,3港币,4越南盾,5新加坡币,6英镑,7欧元,8比特币
     */
    private String currencyCode;

    /**
     * 备注
     */
    private String remark;
    /**
     * 创建用户
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改用户
     */
    private String modifyUser;
    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 0:未删除，1：已删除
     */
    private String delFlag;
    /**
     * 商户编码
     */
    private String merchantCode;

    private String ip;
    /**
     *
     */
    private BigDecimal profit;


    private BigDecimal betAmount;

    /**
     * 名称(普通、白银、黄金、钻石)
     */
    @TableField(exist = false)
    private String levelName;
}
