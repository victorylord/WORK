package com.panda.sport.rcs.trade.wrapper.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.MerchantMapper;
import com.panda.sport.rcs.pojo.Merchant;
import com.panda.sport.rcs.trade.wrapper.MerchantService;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.data.service.impl
 * @Description :  TODO
 * @Date: 2020-09-29 17:02
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

	@Override
	public Merchant getByUid(String userId) {
		return baseMapper.getByUid(userId);
	}
}
