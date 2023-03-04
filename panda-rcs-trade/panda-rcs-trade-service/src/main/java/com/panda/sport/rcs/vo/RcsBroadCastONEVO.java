package com.panda.sport.rcs.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-09 11:42
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RcsBroadCastONEVO {
    /**
     * 是否已读  null 未读
     */
    private Integer  isRead;
    /**
     * 数量
     */
    private Integer  count;

}
