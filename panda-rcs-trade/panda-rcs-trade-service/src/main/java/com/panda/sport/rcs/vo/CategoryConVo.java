package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  玩法集
 * @Date:
 */
@Data
public class CategoryConVo implements Serializable {
    /**
     * 玩法集ID
     **/
    private Long id;
    /**
     * 赛事种类
     */
    private Long sportId;
    /**
     * 玩法集名称
     */
    private String name;
    /**
     * 玩法集排序值
     */
    private Integer orderNo;
    /**
     * 玩法id集合
     */
    private String categoryIds;

    public List<Long> categoryIds() {
        return JsonFormatUtils.fromJsonArray(this.getCategoryIds(), Long.class);
    }
}
