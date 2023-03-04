package com.panda.sport.rcs.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.pojo.RcsTraderSuggest;
import com.panda.sport.rcs.pojo.dto.RcsTraderSuggestDto;
import com.panda.sport.rcs.vo.OrderDetailVo;

/**
 * <p>
 * 操盘手贴标提醒风控 Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2022-04-08
 */
public interface RcsTraderSuggestMapper extends BaseMapper<RcsTraderSuggest> {

	IPage<RcsTraderSuggestDto> selectPage(Page<OrderDetailVo> page,
			@Param("dto") RcsTraderSuggestDto rcsTraderSuggestDto);

}
