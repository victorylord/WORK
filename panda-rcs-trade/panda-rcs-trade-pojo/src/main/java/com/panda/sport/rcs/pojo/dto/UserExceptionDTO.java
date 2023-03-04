package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  vector 2.3
 * @Project Name :  data-realtime
 * @Package Name :  com.panda.sport.data.realtime.api.message
 * @Description :  TODO
 * @Date: 2019-10-07 17:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class UserExceptionDTO implements Serializable {
    /**
     * 用户名称
     */
    private String user;

    /**
     *查询类型 0 前端显示；1导出报表
     * */
    private Integer category;

    /**
     * 从哪个通首来
     * -1 表示只推送时间
     */
    private String merchantCode;
    /**
     * 商户列表名称
     * */
    private List<String> merchantCodes;
    /**
     * 开始时间
     * */
    private long startTime;
    /**
     * 结束时间
     * */
    private long endTime;
    /**
     * 每页大少
     * */
    private Integer pageSize;
    /**
     * 每页页数
     * */
    private Integer pageNum;

}
