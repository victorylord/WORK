package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.vo.LogData;
import lombok.Data;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-10 15:03
 **/
@Data
public class RcsOperationLogHistory {
    private Integer id;
    private String uid;
    private String userName;
    private String name;
    private String type;
    private String updateContent;
    private List<LogData> logDataList;
    private String crtTime;
    private String trader;
}
