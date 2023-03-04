package com.panda.sport.rcs.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsTraderSuggest;
import com.panda.sport.rcs.pojo.dto.RcsTraderSuggestDto;

/**
 * <p>
 * 操盘手贴标提醒风控 服务类
 * </p>
 *
 * @author ${author}
 * @since 2022-04-08
 */
public interface RcsTraderSuggestService extends IService<RcsTraderSuggest> {

	IPage<RcsTraderSuggestDto> list(RcsTraderSuggestDto rcsTraderSuggestDto);

}
