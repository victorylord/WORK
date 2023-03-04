package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.pojo.vo.ThirdDataSourceCodeVo;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class RcsTournamentTemplateAcceptConfig extends BaseTournament {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 玩法集id
     */
    private Integer categorySetId;
    /**
     * SR  BC  BG
     */
    private String dataSource;

    /**
     * T常规
     */
    private Integer normal;

    /**
     * T延时
     */
    private Integer minWait;

    /**
     * 最大延时
     */
    private Integer maxWait;
    /**
     * 最大延时
     */
    @TableField(exist = false)
    private List<RcsTournamentTemplateAcceptEvent> events;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date createTime;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date updateTime;

    /**
     * 复制的玩法集id
     */
    @TableField(exist = false)
    private Integer copyCategorySetId;

    /**
     * 复制的玩法集名称
     */
    @TableField(exist = false)
    private String copyCategorySetName;
    /**
     * 三方赛事商业数据源
     */
    @TableField(exist = false)
    private List<ThirdDataSourceCodeVo> thirdDataSourceCode;
    /**
     * 操作頁面代碼
     */
    @TableField(exist = false)
    private Integer operatePageCode;
    /**
     * 模板名稱
     */
    @TableField(exist = false)
    private String templateName;

    @TableField(exist = false)
    private Map<String, Object> beforeParams;
    /**
     * 玩法集名稱
     */
    @TableField(exist = false)
    private String categorySetName;
}
