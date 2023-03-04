package com.panda.sport.rcs.pojo.dto;


import com.panda.sport.rcs.core.bean.RcsBaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.pojo.dto
 * @Description :  TODO
 * @Date: 2020-10-18 14:45
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO extends RcsBaseEntity<MessageDTO> {
    /**
     *  id
     **/
    private Long id;
    /**
     *  消息类型
     **/
    private Integer msgType;
    /**
     *  消息标题
     **/
    private String msgTitle;
    /**
     *  赛事id
     **/
    private Integer matchId;
    /**
     *  消息内容
     **/
    private String msg;
    /**
     *  消息唯一id
     **/
    private String msgId;
    /**
     *  消息生成时间
     **/
    private String sendTime;
    /**
     *  是否已读 1已经读取  0或者null未读
     **/
    private Integer isRead;
    /**
     * 体育种类id
     */
    private Integer sportId;
}
