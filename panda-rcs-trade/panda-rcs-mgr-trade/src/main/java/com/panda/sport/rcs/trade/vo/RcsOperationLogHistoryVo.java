package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.vo.RcsOperationLogHistory;
import lombok.Data;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-10 14:43
 **/
@Data
public class RcsOperationLogHistoryVo {
    private Long total;
    private Integer currentDayCount = 0;
    private Long pageSize;
    private Long pageNum;
    private  List<RcsOperationLogHistory> rcsOperationLogHistoryList;
}
