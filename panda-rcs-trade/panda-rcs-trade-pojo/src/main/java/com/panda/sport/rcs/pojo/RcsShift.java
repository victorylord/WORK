package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RcsShift {
    private Long id;

    /**
     * 球种0:未分 1:足球  2:篮球  300:综合球种
     */
    private Integer sportId;

    /**
     * 0:滚球 1:早盘 10:早盘欧洲 20:早盘亚洲 30:早盘美洲 40:柬埔寨 100:未分
     */
    private Integer marketType;

    /**
     * 账号(注意为员工的英文名)
     */
    private String userCode;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 班次名 0:未分 10:早班 20:早班1 30:早班2 40:中班 50:中班1 60:中班2 70:晚班 80:晚班1 90:晚班2
     */
    private String shift;

    private Date createTime;

    private Date updateTime;
    @TableField(exist = false)
    private String title;
    @TableField(exist = false)
    private List children;
    /**
     * 父级
     */
    @TableField(exist = false)
    private String supers;
    @TableField(exist = false)
    private Boolean active = false;
}