package com.panda.rcs.warning.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author :  carver
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :   多语言
 * @Date: 2021-01-30 16:40
 */
@Data
public class RcsLanguageInternation implements Serializable {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文字对应的编码
     */
    private Long nameCode;

    /**
     * json  language_type,text
     */
    private String text;

    private Date createTime;

    private Date updateTime;
}