package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * @Description   //用户等级
 * @Param
 * @Author  Sean
 * @Date  14:03 2020/9/30
 * @return
 **/
@Data
public class TUserLevel extends RcsBaseEntity<TUserLevel> {
    /**
     * 版本号
     */
    private static final long serialVersionUID = -5591067906256831859L;
    /**
     * 等级id
     */
    @TableId
    private Integer levelId;
    /**
     * 状态，1为有效。0为无效，默认1
     */
    private Integer status;
    /**
     * 等级名称
     */
    private String levelName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 底色
     */
    private String bgColor;
    /**
     * 颜色
     */
    private String color;

}
