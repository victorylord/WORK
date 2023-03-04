package com.panda.sport.rcs.trade.wrapper.impl;

import java.util.Arrays;
import java.util.List;

import com.panda.sport.rcs.pojo.TOrderForChampion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.TOrderMapper;
import com.panda.sport.rcs.pojo.TOrder;
import com.panda.sport.rcs.trade.wrapper.ITOrderService;

import lombok.extern.slf4j.Slf4j;


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
public class TOrderServiceImpl<slf4j> extends ServiceImpl<TOrderMapper, TOrder> implements ITOrderService {
    @Autowired
    private TOrderMapper orderMapper;
    /**
     * @Description   通过订单明细扩展表
     * @Param [date]
     * @Author  toney
     * @Date  15:56 2020/1/31
     * @return java.util.List<com.panda.sport.rcs.pojo.TOrder>
     **/
    @Override
    public List<TOrderForChampion> queryByOrderDetailExtAndIds(String[] ids){
        return orderMapper.queryByOrderDetailExtAndIds(Arrays.asList(ids));
    }

    /**
     * @Author: Kir
     * @deprecated 根据订单ID查询赛事ID
     * @param orderNo
     * @Date 2021/1/5
     * @return
     */
    @Override
    public Long selectMatchIdByOrderNo(String orderNo){
        return orderMapper.selectMatchIdByOrderNo(orderNo);
    }

}
