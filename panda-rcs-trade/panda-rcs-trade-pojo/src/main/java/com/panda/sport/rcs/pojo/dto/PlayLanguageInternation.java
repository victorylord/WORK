package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.pojo.LanguageInternation;
import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.pojo.dto
 * @Description :  TODO
 * @Date: 2020-08-08 11:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class PlayLanguageInternation extends LanguageInternation {
    /**
     * 玩法id
     **/

    private Integer playId;

    private Integer categoryId;

    private Integer sportId;
}
