package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.common.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author :  Ming
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  订单明细扩展表
 * @Date: 2022-01-10 13:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Document("t_order_detail_ext")
@Data
@EqualsAndHashCode(callSuper = false)
public class TOrderDetailExtDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @Description id
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private String id;
    /**
     * @Description betNo
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private String betNo;
    /**
     * @Description 订单编号
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private String orderNo;
    /**
     * @Description 赛事Id
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Long matchId;
    private Integer categorySetId;
    /**
     * @Description 处理状态 0 待处理 1 接单  2拒单
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return

     **/
    private Integer orderStatus;
    /**
     * @Description 下单时间
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Long betTime;
    /**
     * 预期最迟接单时间
     */
    private Long maxAcceptTime;
    private Long finalAcceptTime;
    /**
     * @Description 当前事件最长接单等待时长
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Integer maxWait;
    /**
     * @Description   //最小接单时间
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Integer minWait;
    /**
     * @Description 当前事件
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private String currentEvent;
    /**
     * @Description   事件类型0-安全;1-危险;2-封盘;3拒单
     * @Param
     * @Author  sean
     * @Date   2020/11/6
     * @return
     **/
    private Integer currentEventType;
    /**
     * @Description 创建时间
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Date crtTime;
    /**
     * @Description 更新时间
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Date updateTime;

    /**
     * @Description 0 自动 1 手动
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Integer mode;
    /**
     * @Description 订单扫描状态 0 未处理 1 已处理 2处理中
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Integer handleStatus;
    /**
     * @Description   //字符串时间类型
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    public String getUpdateTimeStr(){
        return DateUtils.transferLongToDateStrings(System.currentTimeMillis());
    }

    /**
     * @Description 寫入MongoDB時間
     * @Param
     * @Author Ming
     * @Date 13:00 2022/1/10
     * @return
     **/
    private Long createTime;
}
