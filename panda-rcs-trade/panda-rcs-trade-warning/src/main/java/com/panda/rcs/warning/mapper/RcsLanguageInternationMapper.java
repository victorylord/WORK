package com.panda.rcs.warning.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.rcs.warning.vo.LanguageInternation;
import com.panda.rcs.warning.vo.RcsLanguageInternation;


@Component
public interface RcsLanguageInternationMapper extends BaseMapper<RcsLanguageInternation> {

	/**
	 * 	根据
	 * 
	 * @param lang
	 * @param nameCodeList
	 * @return
	 */
	List<LanguageInternation> selectByLanguageTypeAndNameCodes(@Param("lang") String lang, @Param("nameCodeList") List<Long> nameCodeList);
}