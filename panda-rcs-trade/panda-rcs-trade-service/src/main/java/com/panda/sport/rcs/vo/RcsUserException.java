package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.vo.LogData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-10 15:03
 **/
@Data
public class RcsUserException implements Serializable {
    private Integer id;
    private String uid;
    private String type;
    private String userName;
    private String merchantCode;
    private String updateContent;
    private List<String> updateContents;
    private String crtTime;
}
