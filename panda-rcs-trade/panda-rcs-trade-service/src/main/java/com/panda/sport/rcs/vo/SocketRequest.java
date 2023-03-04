package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * Socket数据包，表示推送的数据
 *
 * @param <T>
 */
@Data
public class SocketRequest<T> implements java.io.Serializable {

    /**
     * 数据版本号，用于确保前端数据是最新的，否则需要主动拉取
     */
    private Long versionNo;

    /**
     * 发送的数据
     */
    private T data;

}
