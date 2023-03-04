package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.TTagMarketMapper;
import com.panda.sport.rcs.pojo.TTagMarket;
import com.panda.sport.rcs.pojo.TTagMarketReqVo;
import com.panda.sport.rcs.trade.wrapper.TTagMarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-03
 */
@Service
@Slf4j
public class TTagMarketServiceImpl<slf4j> extends ServiceImpl<TTagMarketMapper, TTagMarket> implements TTagMarketService {

    @Autowired
    private TTagMarketMapper marketMapper;

    @Override
    public List<TTagMarketReqVo> getTagMarketList() {
        return marketMapper.getTagMarketList();
    }
}
