package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.pojo.vo.LogData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * rcs_operation_log 日志
 * @author 
 */
@Data
public class RcsOperationLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 操作编码
     */
    private String handleCode;

    private String hanlerId;

    /**
     * 修改前的内容
     */
    private String updatePreContent;

    /**
     * 修改后的内容
     */
    private String updateContent;
    /**
     * 修改后的内容
     */
    @TableField(exist = false)
    private List<LogData> updateContentList;

    /**
     * 显示的内容
     */
    private String showContent;

    private String crtTime;
    @TableField(exist = false)
    private Long crtTimeDate;

    private String updateTime;

    private static final long serialVersionUID = 1L;
}