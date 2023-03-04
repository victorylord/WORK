package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.Merchant;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.data.service
 * @Description :  TODO
 * @Date: 2020-09-29 17:02
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface MerchantService extends IService<Merchant> {

	Merchant getByUid(String userId);
}
