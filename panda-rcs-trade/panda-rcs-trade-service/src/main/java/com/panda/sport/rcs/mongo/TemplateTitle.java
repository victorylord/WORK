package com.panda.sport.rcs.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.mongo
 * @Description : 模板标题
 * @Author : Paca
 * @Date : 2020-07-26 16:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TemplateTitle implements Serializable {

    private static final long serialVersionUID = -2944330712360341131L;

    /**
     * 位置
     */
    private Integer index;

    /**
     * 0-否，1-是
     */
    private Integer isOther = 0;

    /**
     * 标题名称，多语言
     */
    private I18nBean names;

    public TemplateTitle setName(String name) {
        if (this.names == null) {
            this.names = new I18nBean(name);
        }
        return this;
    }

}
