package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsUserConfigExt;
import com.panda.sport.rcs.pojo.RcsUserRemarkRemindLog;
import com.panda.sport.rcs.trade.vo.RcsMerchantUserTagMarketLevelStatusReqVo;
import com.panda.sport.rcs.trade.vo.RcsUserConfigExtReqVo;

/**
 * 用戶人工备注提醒日志
 *
 * @description:
 * @author: magic
 * @create: 2022-05-29 10:15
 **/
public interface IRcsUserRemarkRemindLogService extends IService<RcsUserRemarkRemindLog> {

    /**
     * 修改人工备注提醒  需求：1874
     *
     * @return
     */
    void updateRemark(RcsUserRemarkRemindLog rcsUserRemarkRemindLog,int traderId);
}
