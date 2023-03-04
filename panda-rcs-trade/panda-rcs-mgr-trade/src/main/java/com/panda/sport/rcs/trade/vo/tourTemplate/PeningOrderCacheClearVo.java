package com.panda.sport.rcs.trade.vo.tourTemplate;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 清理缓存 通知参数
 * @author: lithan
 * @date: 2020-10-15 14:49
 **/
@Data
public class PeningOrderCacheClearVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 体育种类
     */
    private String template;
    private String typeVal;
    private Integer matchType;
    private String  marginRef;
    private Integer playId;
    private String  matchId;
    private String dataType;
    private Integer sportId;
}