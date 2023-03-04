package com.panda.rcs.warning.vo;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;

/**
 * 	language_internation 此表不做维护，已弃用，此类仅为传输类，原有返回前端的数据结构不变，查询Rcs_Language_Internation表数据
 * 
 * @ClassName LanguageInternation
 * @Description: TODO 
 * @Author Vector
 * @Date 2019/10/8 
**/
@Data
public class LanguageInternation implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;

    /**
    * 文字对应的编码
    */
    private Long nameCode;

    /**
    * 数据来源编码. SR BC等
    */
    private String dataSourceCode;

    /**
    * 语言类型. zh jp en 等
    */
    private String languageType;

    /**
    * 文字内容.  name_code 代表文字在 language_type代表语言下的 具体内容.比如:中国 在 英文的表示  是China. 
    */
    private String text;

    private String remark;

    private Long createTime;

    private Long modifyTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LanguageInternation that = (LanguageInternation) o;
        return  Objects.equals(nameCode, that.nameCode) &&
                Objects.equals(languageType, that.languageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),  nameCode, languageType);
    }
}