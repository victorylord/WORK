package com.panda.sport.rcs.trade.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsTraderSuggestMapper;
import com.panda.sport.rcs.pojo.RcsTraderSuggest;
import com.panda.sport.rcs.pojo.dto.RcsTraderSuggestDto;
import com.panda.sport.rcs.trade.service.RcsTraderSuggestService;
import com.panda.sport.rcs.vo.OrderDetailVo;

/**
 * <p>
 * 操盘手贴标提醒风控 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2022-04-08
 */
@Service
public class RcsTraderSuggestServiceImpl extends ServiceImpl<RcsTraderSuggestMapper, RcsTraderSuggest>
		implements RcsTraderSuggestService {

	@Override
	public IPage<RcsTraderSuggestDto> list(RcsTraderSuggestDto rcsTraderSuggestDto) {
		Page<OrderDetailVo> page = new Page<>(rcsTraderSuggestDto.getPageNum(), rcsTraderSuggestDto.getPageSize());
		IPage<RcsTraderSuggestDto> selectPage = baseMapper.selectPage(page, rcsTraderSuggestDto);
		List<RcsTraderSuggestDto> records = selectPage.getRecords();
		for (RcsTraderSuggestDto target : records) {
			String userName = target.getUserName();
			userName= userName.substring(userName.substring(0, userName.indexOf("_")).length()+1, userName.length());
			target.setUserName(userName);
		}
		selectPage.setRecords(records);
		return baseMapper.selectPage(page, rcsTraderSuggestDto);
	}

}
