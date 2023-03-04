package com.panda.sport.rcs.trade.service.api;

import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.pojo.dto.UserExceptionDTO;
import com.panda.sport.rcs.pojo.dto.api.RcsTradeRestrictMerchantSettingDto;
import com.panda.sport.rcs.pojo.dto.api.UserIdDto;
import com.panda.sport.rcs.pojo.dto.api.UserSpecialLimitDto;
import com.panda.sport.rcs.vo.RcsUserExceptionVo;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 用户限额服务
 * @Author : Paca
 * @Date : 2021-08-17 22:16
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface UserLimitApiService {

    /**
     * 修改用户特殊限额
     *
     * @param request
     * @return
     */
    Response<Boolean> updateUserSpecialLimit(Request<UserSpecialLimitDto> request);

	/**
	 * 	获取操盘手配置投注额外延 标签行情等级ID
	 * @param userIdDto
	 * @return
	 */
	Response<RcsTradeRestrictMerchantSettingDto> getUserTradeRestrict(UserIdDto userIdDto);
    /**
	 * 查询错误线上日志
	 * @param req
	 * @return
	 * */
	Response<RcsUserExceptionVo> queryUserExceptionByOnline(UserExceptionDTO req);

}