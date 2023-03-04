package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  Vito
 * @Project Name :  panda_data_nonrealtime
 * @Package Name :  com.panda.sport.data.nonrealtime.api.dto
 * @Description :  国际化
 * @Date: 2019-08-31 9:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class I18nItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 语言类型。 zh jp en 等
     */
    private String languageType;

    /**
     * 文字内容。  name_code 代表文字在 language_type代表语言下的 具体内容。比如：中国 在 英文的表示  是China。
     */
    private String text;
}
