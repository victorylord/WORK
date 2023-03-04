package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.Merchant;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.data.mapper
 * @Description :  TODO
 * @Date: 2020-09-29 17:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface MerchantMapper extends BaseMapper<Merchant> {

	Merchant getByUid(String userId);
}
