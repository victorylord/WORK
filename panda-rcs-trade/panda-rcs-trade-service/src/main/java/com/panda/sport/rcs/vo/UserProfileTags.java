package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-09 12:06
 **/
@Data
public class UserProfileTags implements Serializable {
    /**
     *
     */
    private Long id;
    /**
     * 标签类型  2投注特征类
     */
    private Integer tagType;
    /**
     * 标签名称
     */
    private String tagName;
    /**
     * 标签说明
     */
    private String tagDetail;
    /**
     * 标签颜色
     */
    private String tagColor;
    /**
     * 标签图标
     */
    private String tagImgUrl;
}
