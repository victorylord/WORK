package com.panda.sport.rcs.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.pojo.vo
 * @Description :  TODO
 * @Date: 2020-10-18 16:35
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsBroadCastVo implements Serializable {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息类型：1：预警消息  2结算消息  3封盘消息
     */
    private Integer msgType;

    /**
     * 消息id：组合键
     */
    private String msgId;

    /**
     * 广播内容
     */
    private String content;

    /**
     * 扩展字段
     */
    private String extendsField;
    /**
     * 扩展字段
     */
    private String extendsField1;

    /**
     * 扩展字段
     */
    private String extendsField2;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 过期时间
     */
    private String expiresTime;

    /**
     * 创建用户
     */
    private String crtUser;

    /**
     * 接收用户
     */
    private String tagUser;

    /**
     * 1：已读 0：未读
     */
    private Integer isRead;
    /**
     * 是否有效 1:有效
     */
    private Integer status;
    /**
     * 体育种类id
     */
    private Integer sportId;
}
