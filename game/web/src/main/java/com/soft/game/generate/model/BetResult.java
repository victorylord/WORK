/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.generate.model;

import java.util.Date;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "bet_result")
public class BetResult {
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 投注时间
     */
    @Column(name = "bet_time")
    private Date betTime;

    /**
     * 订单编号
     */
    @Column(name = "order_no")
    private Long orderNo;

    /**
     * 投注金额
     */
    @Column(name = "bet_money")
    private Long betMoney;

    /**
     * 开奖结果
     */
    @Column(name = "draw_result")
    private String drawResult;

    /**
     * 得分
     */
    @Column(name = "score")
    private Long score;

    /**
     * 账号
     */
    @Column(name = "account")
    private String account;
}