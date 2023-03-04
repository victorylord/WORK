package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsOddsConvertMapping;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 赔率转换映射表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Repository
public interface RcsOddsConvertMappingMapper extends BaseMapper<RcsOddsConvertMapping> {

	List<RcsOddsConvertMapping> queryOddsMappingList();
	/**
	 * @Description   //根据马来赔获取最大的欧赔
	 * @Param [myOdds]
	 * @Author  Sean
	 * @Date  11:17 2020/10/17
	 * @return java.lang.String
	 **/
	String queryMaxOdds(@Param("myOdds")String myOdds);
	/**
	 * @Description   //根据马来赔获取最小的欧赔
	 * @Param [myOdds]
	 * @Author  Sean
	 * @Date  11:17 2020/10/17
	 * @return java.lang.String
	 **/
	String queryMinOdds(@Param("myOdds")String myOdds);

}
