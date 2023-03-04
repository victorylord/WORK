package com.panda.sport.rcs.trade.controller.api;

import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.pojo.dto.UserExceptionDTO;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.RcsUserException;
import com.panda.sport.rcs.vo.RcsUserExceptionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.pojo.dto.api.RcsTradeRestrictMerchantSettingDto;
import com.panda.sport.rcs.pojo.dto.api.UserIdDto;
import com.panda.sport.rcs.pojo.dto.api.UserSpecialLimitDto;
import com.panda.sport.rcs.trade.service.api.UserLimitApiService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : api
 * @Author : Paca
 * @Date : 2021-08-18 19:40
 * @ModificationHistory Who When What -------- ---------
 *                      --------------------------
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/trade")
@Api(tags = "提供给业务的接口")
public class ApiController {

	@Autowired
	private UserLimitApiService userLimitApiService;

	/**
	 * 修改用户特殊限额
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/updateUserSpecialLimit")
	@ApiOperation("修改用户特殊限额")
	public Response<Boolean> updateUserSpecialLimit(@RequestBody Request<UserSpecialLimitDto> request) {
		return userLimitApiService.updateUserSpecialLimit(request);
	}

	/**
	 * 	获取操盘手配置投注额外延 标签行情等级ID 特殊百分比限额
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/getUserTradeRestrict")
	@ApiOperation("获取操盘手配置投注额外延 标签行情等级ID 特殊百分比限额")
	public Response<RcsTradeRestrictMerchantSettingDto> getUserTradeRestrict(
			@RequestBody Request<UserIdDto> request) {
		if (request == null) {
			return Response.error(Response.FAIL, "request参数不能为空", false);
		}
		UserIdDto userIdDto = request.getData();
		if (userIdDto == null) {
			return Response.error(Response.FAIL, "data参数不能为空", false);
		}
		return userLimitApiService.getUserTradeRestrict(request.getData());
	}
	/**
	 * 	获取操盘手配置投注额外延 标签行情等级ID 特殊百分比限额
	 *
	 * @param request
	 * @return
	 */
	@PostMapping("/queryUserExceptionByOnline")
	@ApiOperation("获取操盘手配置投注额外延 标签行情等级ID 特殊百分比限额")
	public Response<RcsUserExceptionVo> queryUserExceptionByOnline(@RequestBody UserExceptionDTO request) {
		if (request == null) {
			return Response.error(Response.FAIL, "request参数不能为空", false);
		}
		if(request.getPageSize()<=0){
			log.warn("::{}::页数必须大于1 当前为PageSize{}", CommonUtil.getRequestId(), request.getPageSize());
			return Response.error(Response.FAIL,"页数必须大于1",false);
		}
		return userLimitApiService.queryUserExceptionByOnline(request);
	}
}
