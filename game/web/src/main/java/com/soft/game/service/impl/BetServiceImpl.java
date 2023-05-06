/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.soft.game.dao.BetDao;
import com.soft.game.generate.mapper.AccountMapper;
import com.soft.game.generate.mapper.BetJournalMapper;
import com.soft.game.generate.mapper.BetResultMapper;
import com.soft.game.generate.model.Account;
import com.soft.game.generate.model.AccountExample;
import com.soft.game.generate.model.BetJournal;
import com.soft.game.generate.model.BetResult;
import com.soft.game.generate.model.BetResultExample;
import com.soft.game.model.BetOnce;
import com.soft.game.model.BetRequest;
import com.soft.game.model.BetResponse;
import com.soft.game.model.BetStakEeffectRequest;
import com.soft.game.model.BetStakEeffectResponse;
import com.soft.game.service.BetService;
import com.soft.game.socket.message.SocketMessageOpType;
import com.soft.game.utils.pk.DistributeIdGenerator;

import lombok.extern.slf4j.Slf4j;

@Service
@Component
@Slf4j
public class BetServiceImpl implements BetService {
    @Autowired
    BetJournalMapper betJournalMapper;
    @Autowired
    BetResultMapper betResultMapper;
    @Autowired
    AccountMapper accountMapper;
    @Autowired
    BetDao betDao;
    @Autowired
    RedissonClient redissonClient;

    @Override
    public BetResponse bet(BetRequest betRequest) {

        BetOnce betOnce = betOnce();
        BetResponse result = new BetResponse();
        result.setPerm(betOnce.getPerm());
        result.setScore(betOnce.getScore());
        result.setPrize(betRequest.getTotalBet() * betOnce.getScore());
        Long orderNo = DistributeIdGenerator.getInstance().nextId();
        result.setOrderNo(orderNo);
        Date betTime = new Date();
        betRecord(orderNo, betTime, betRequest.getAccount(),
                betRequest.getTotalBet(), JSON.toJSONString(betOnce.getPerm()), betOnce.getScore());

        return result;
    }

    @Override
    public BetStakEeffectResponse betStakEeffect(BetStakEeffectRequest betStakEeffectRequest) {
        if (betStakEeffectRequest.getOrderNo() == null) {
            return new BetStakEeffectResponse();
        }

        BetResultExample betResultExample = new BetResultExample();
        betResultExample.createCriteria().andOrderNoEqualTo(betStakEeffectRequest.getOrderNo());
        List<BetResult> betResultList = betResultMapper.selectByExample(betResultExample);
        if (CollectionUtils.isEmpty(betResultList)) {
            return new BetStakEeffectResponse();
        }
        BetResult betResult = betResultList.get(0);
        if (betResult == null || betResult.getAccount() == null) {
            return new BetStakEeffectResponse();
        }
        String betKey = SocketMessageOpType.BETSTAKEEFFECT + "_" + betResult.getAccount();
        RLock rLock = redissonClient.getLock(betKey);
        try {
            rLock.lock(10, TimeUnit.SECONDS);
            Long prize = betResult.getBetMoney() * betResult.getScore();
            Date betTime = new Date();
            betJournal(betStakEeffectRequest.getOrderNo(), betTime, betResult.getAccount(), betResult.getBetMoney(),
                    prize);
            Account currAccount = account(betResult.getAccount(), betResult.getBetMoney(), prize);
            BetStakEeffectResponse result = new BetStakEeffectResponse();
            result.setBalance(currAccount.getBalance() == null ? 0 : currAccount.getBalance());
            return result;
        } finally {
            rLock.unlock();
        }
    }

    private BetOnce betOnce() {
        int score = 0;
        Random random = new Random();
        int a = random.nextInt(10);
        int b = random.nextInt(10);
        int c = random.nextInt(10);
        if (a == 0 && b == 0 && c == 0) {
            score = 800;
        }
        if (a == 4 && b == 4 && c == 4) {
            score = 400;
        }
        if (a == 6 && b == 6 && c == 6) {
            score = 250;
        }
        if (a == 8 && b == 8 && c == 8) {
            score = 100;
        }
        if (a == 2 && b == 2 && c == 2) {
            score = 100;
        }
        if ((a == 2 && b == 2) || (c == 2 && b == 2) || (c == 2 && a == 2)) {
            score = 30;
        }
        if (a == 2 || b == 2 || c == 2) {
            score = 15;
        }
        BetOnce result = new BetOnce();
        List<Long> perm = new ArrayList<>();
        perm.add(Long.valueOf(a));
        perm.add(Long.valueOf(b));
        perm.add(Long.valueOf(c));
        result.setPerm(perm);
        result.setScore(Long.valueOf(score));
        return result;
    }

    public void betRecord(Long orderNo, Date betTime, String account,
                          Long betMoney, String drawResult, Long score) {
        BetResult result = new BetResult();
        result.setId(DistributeIdGenerator.getInstance().nextId());
        result.setOrderNo(orderNo);
        result.setBetTime(betTime);
        result.setAccount(account);
        result.setBetMoney(betMoney);
        result.setDrawResult(drawResult);
        result.setScore(score);
        betResultMapper.insert(result);
    }

    public void betJournal(Long orderNo, Date betTime, String account, Long betMoney, Long prize) {
        BetJournal journal = new BetJournal();
        journal.setId(DistributeIdGenerator.getInstance().nextId());
        journal.setOrderNo(orderNo);
        journal.setBetTime(betTime);
        journal.setAccount(account);
        journal.setBetMoney(betMoney);
        journal.setPrize(prize);
        betJournalMapper.insert(journal);
    }

    public Account account(String account, Long betMoney, Long prize) {
        AccountExample accountExample = new AccountExample();
        accountExample.createCriteria().andAccountEqualTo(account);
        Account currAccount = accountMapper.selectOneByExample(accountExample);
        if (Objects.isNull(currAccount)) {
            return new Account();
        }
        currAccount.setBalance(
                currAccount.getBalance() - (betMoney == null ? 0L : betMoney) + (prize == null ? 0L : prize));
        return currAccount;
        // betDao.updateAccount(account, betMoney == null ? 0L : betMoney, prize == null ? 0L : prize);
    }

}
