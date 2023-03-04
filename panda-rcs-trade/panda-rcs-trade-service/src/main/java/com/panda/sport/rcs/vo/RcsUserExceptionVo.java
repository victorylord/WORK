package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program:
 * @description:
 * @author: skykong
 * @create: 2022-07-16 15:03
 **/
@Data
public class RcsUserExceptionVo implements Serializable {
    private Integer total;//总数
    private Integer outPutTotal;//总页数
    List<RcsUserException> rcsUserExceptions=null; //
}
