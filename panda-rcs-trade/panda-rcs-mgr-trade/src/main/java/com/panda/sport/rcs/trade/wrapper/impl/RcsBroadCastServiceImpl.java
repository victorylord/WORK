package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsBroadCastMapper;
import com.panda.sport.rcs.pojo.RcsBroadCast;
import com.panda.sport.rcs.pojo.vo.OperateMessageVo;
import com.panda.sport.rcs.pojo.vo.RcsBroadCastVo;
import com.panda.sport.rcs.trade.wrapper.RcsBroadCastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2020-03-05 16:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsBroadCastServiceImpl extends ServiceImpl<RcsBroadCastMapper, RcsBroadCast> implements RcsBroadCastService {

    @Autowired
    private RcsBroadCastMapper rcsBroadCastMapper;

    @Override
    public List<RcsBroadCastVo> queryRcsBroadCastVo(Integer pageNum, Integer pageSize, Integer userId, List<Integer> sportIdList, Long time, Integer isTrade) {
        return rcsBroadCastMapper.queryRcsBroadCastVo((pageNum-1) * pageSize,pageSize,userId, sportIdList, time,isTrade);
    }

    @Override
    public OperateMessageVo queryRcsBroadCastVoIsNoRead(Integer userId, List<Integer> sportIdList, Long time,Integer isTrade) {
        return rcsBroadCastMapper.queryRcsBroadCastVoIsNoRead(userId, sportIdList, time,isTrade);
    }
}
