package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsBroadCast;
import com.panda.sport.rcs.pojo.vo.RcsBroadCastVo;
import com.panda.sport.rcs.pojo.vo.OperateMessageVo;

import java.util.List;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  预警消息
 * @Date: 2020-09-16 16:38
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsBroadCastService extends IService<RcsBroadCast> {

    List<RcsBroadCastVo> queryRcsBroadCastVo(Integer pageNum,Integer pageSize,Integer userId, List<Integer> sportIdList, Long time,Integer isTrade);

    OperateMessageVo queryRcsBroadCastVoIsNoRead(Integer userId, List<Integer> sportIdList, Long time,Integer isTrade);
}
