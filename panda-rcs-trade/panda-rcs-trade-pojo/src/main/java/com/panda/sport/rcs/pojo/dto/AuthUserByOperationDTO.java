package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.pojo.RcsBroadCast;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.pojo.dto
 * @Description :  TODO
 * @Date: 2020-10-22 16:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class AuthUserByOperationDTO {
    /**
     * 消息
     **/
    MessageDTO messageDTO;
    /**
     * 有权限的玩家id
     **/
    Set<Integer> userId;
}
