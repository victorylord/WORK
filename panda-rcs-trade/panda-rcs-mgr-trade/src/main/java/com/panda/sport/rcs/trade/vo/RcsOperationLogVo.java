package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.pojo.RcsOperationLog;
import lombok.Data;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-06 15:42
 **/
@Data
public class RcsOperationLogVo {
    private List<RcsOperationLog> rcsOperationLogList;
    private Integer total;

}
