package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsUserConfigExt;
import com.panda.sport.rcs.trade.vo.RcsMerchantUserTagMarketLevelStatusReqVo;
import com.panda.sport.rcs.trade.vo.RcsUserConfigExtReqVo;

/**
 * 用户特殊配置扩展
 *
 * @description:
 * @author: magic
 * @create: 2022-05-14 18:15
 **/
public interface IRcsUserConfigExtService extends IService<RcsUserConfigExt> {

    /**
     * 修改用户赔率分组动态风控开关
     * 需求：1782
     * http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=63015150
     *
     * @param rcsUserConfigExtReqVo
     * @param traderId 操作人
     */
    void saveTagMarketLevelStatus(RcsUserConfigExtReqVo rcsUserConfigExtReqVo, int traderId);

    /**
     * 批量修改商户下用户赔率分组动态风控开关
     * @param rcsMerchantUserTagMarketLevelStatusReqVo
     * @param traderId
     * @return 总共修改人数
     */
   int batchSaveTagMarketLevelStatus(RcsMerchantUserTagMarketLevelStatusReqVo rcsMerchantUserTagMarketLevelStatusReqVo, int traderId);
}
