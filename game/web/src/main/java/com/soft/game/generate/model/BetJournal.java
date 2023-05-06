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
@Table(name = "bet_journal")
public class BetJournal {
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 投注时间
     */
    @Column(name = "bet_time")
    private Date betTime;

    /**
     * 订单号
     */
    @Column(name = "order_no")
    private Long orderNo;

    /**
     * 投注金额
     */
    @Column(name = "bet_money")
    private Long betMoney;

    /**
     * 奖金
     */
    @Column(name = "prize")
    private Long prize;

    /**
     * 账号
     */
    @Column(name = "account")
    private String account;

    /**
     * 货币
     */
    @Column(name = "currency")
    private String currency;
}