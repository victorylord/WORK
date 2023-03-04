package com.panda.sport.rcs.trade.controller;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.trade.util.CommonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.pojo.Merchant;
import com.panda.sport.rcs.pojo.RcsSysUser;
import com.panda.sport.rcs.pojo.RcsTraderSuggest;
import com.panda.sport.rcs.pojo.dto.RcsTraderSuggestDto;
import com.panda.sport.rcs.trade.service.RcsTraderSuggestService;
import com.panda.sport.rcs.trade.wrapper.MerchantService;
import com.panda.sport.rcs.trade.wrapper.RcsSysUserService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 操盘手贴标提醒风控 前端控制器
 * </p>
 *
 * @author ${author}
 * @since 2022-04-09
 */
@Slf4j
@RestController
@RequestMapping("/riskSuggest")
@Api(tags = "操盘手贴标提醒风控")
public class RcsTraderSuggestController {

	@Autowired
	private RcsTraderSuggestService rcsTraderSuggestService;

	@Autowired
	private RcsSysUserService rcsSysUserService;
	
	@Autowired
	private MerchantService merchantService;

	@PostMapping("/list")
	@ApiOperation("分頁查詢列表")
	@ResponseBody
	public HttpResponse<IPage<RcsTraderSuggestDto>> list(@RequestBody RcsTraderSuggestDto rcsTraderSuggestDto) {
		return HttpResponse.success(rcsTraderSuggestService.list(rcsTraderSuggestDto));
	}

	@ApiOperation("单个保存")
	@PostMapping("/save")
	@LogAnnotion(name = "操盘手贴标提醒风控保存", keys = { "userId", "userName", "businessId", "businessName", "traderSuggest",
			"traderSuggestReplenish" }, title = { "用户id", "用户名", "商户id", "商户名称", "操盘手建议", "操盘手建议补充说明" })
	public HttpResponse<String> save(@RequestBody RcsTraderSuggestDto rcsTraderSuggestDto) throws Exception {
		Assert.notNull(rcsTraderSuggestDto.getUserId(), "用户id不能为空");
		Assert.notNull(rcsTraderSuggestDto.getUserName(), "用户名不能为空");
		Assert.notNull(rcsTraderSuggestDto.getTraderSuggest(), "操盘手建议不能为空");
		RcsTraderSuggest rcsTraderSuggest = new RcsTraderSuggest();
		BeanUtils.copyProperties(rcsTraderSuggestDto, rcsTraderSuggest);
		rcsTraderSuggest.setUserId(Long.parseLong(rcsTraderSuggestDto.getUserId()));
		rcsTraderSuggest.setCreateTime(new Date());
		log.info("::{}::操盘手贴标提醒风控保存:{},操盘手:{}",CommonUtil.getRequestId(rcsTraderSuggestDto.getBusinessId()), JSONObject.toJSONString(rcsTraderSuggestDto), TradeUserUtils.getUserIdNoException());
		// 根据用户id查询商户信息
		Merchant merchant = merchantService.getByUid(rcsTraderSuggestDto.getUserId());
		if (merchant != null) {
			rcsTraderSuggest.setBusinessId(Long.parseLong(merchant.getId()));
			rcsTraderSuggest.setBusinessName(merchant.getMerchantCode());
		}
		// 当前登录人 = 操盘建议人
		RcsSysUser rcsSysUser = rcsSysUserService.getById(TradeUserUtils.getUserIdNoException());
		if (rcsSysUser != null) {
			rcsTraderSuggest.setProposer(rcsSysUser.getUserCode());
		}
		rcsTraderSuggestService.save(rcsTraderSuggest);

		log.info("::{}::操盘手贴标提醒风控新增，参数:{}", CommonUtil.getRequestId(rcsTraderSuggestDto.getBusinessId(),rcsTraderSuggestDto.getUserId()), rcsTraderSuggestDto);
		return HttpResponse.success();
	}

	@ApiOperation("更新状态")
	@PostMapping("/updateStatus")
	@LogAnnotion(name = "操盘手贴标提醒风控更新状态", keys = { "id", "riskProcesStatus", "riskRemark" }, title = { "id",
			"风控处理状态：0:未处理、1:已设置、2:忽略", "风控处理说明" })
	public HttpResponse<String> updateStatus(@RequestBody RcsTraderSuggestDto rcsTraderSuggestDto) {
		Assert.notNull(rcsTraderSuggestDto.getId(), "id不能为空");
		Assert.notNull(rcsTraderSuggestDto.getRiskProcesStatus(), "风控处理状态不能为空");
		RcsTraderSuggest rcsTraderSuggest = new RcsTraderSuggest();
		BeanUtils.copyProperties(rcsTraderSuggestDto, rcsTraderSuggest);
		rcsTraderSuggest.setRiskProcesTime(new Date());
		log.info("::{}::操盘手贴标提醒风控更新状态:{},操盘手:{}",CommonUtil.getRequestId(rcsTraderSuggestDto.getBusinessId()), JSONObject.toJSONString(rcsTraderSuggestDto), TradeUserUtils.getUserIdNoException());
		RcsSysUser rcsSysUser = rcsSysUserService.getById(TradeUserUtils.getUserIdNoException());
		if (rcsSysUser != null) {
			rcsTraderSuggest.setRiskHandler(rcsSysUser.getUserCode());
		}

		rcsTraderSuggestService.updateById(rcsTraderSuggest);
		log.info("::{}::操盘手贴标提醒风控更新狀態，参数:{}",CommonUtil.getRequestId(rcsTraderSuggestDto.getId()),rcsTraderSuggestDto);
		return HttpResponse.success();
	}

}
