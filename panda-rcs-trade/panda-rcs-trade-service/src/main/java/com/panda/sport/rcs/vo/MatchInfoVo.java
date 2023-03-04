package com.panda.sport.rcs.vo;

import lombok.Data;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-12 10:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchInfoVo {
    /**
     * 总页数
     **/
    private Integer pageNum;
    /**
     * 投注项数据
     **/
    private List<OrderDetailVo> orderDetailVoList1;
    /**
     * 投注项数据
     **/
    private List<OrderDetailVo> orderDetailVoList2;
    /**
     * 投注项数据
     **/
    private List<OrderDetailVo> orderDetailVoList3;

    public void setPageNum(Integer pageNum) {
        if (this.pageNum != null) {
            if (pageNum > this.pageNum) {
                this.pageNum = pageNum;
            }
        } else {
            this.pageNum = pageNum;

        }
    }
}
