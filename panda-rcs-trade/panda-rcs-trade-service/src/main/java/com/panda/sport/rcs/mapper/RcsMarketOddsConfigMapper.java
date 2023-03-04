package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMarketNumStatis;
import com.panda.sport.rcs.pojo.RcsMarketOddsConfig;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import com.panda.sport.rcs.vo.OrderDetailStatReportVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description  :  TODO
 * @author       :  Administrator
 * @Date:  2019-11-01 16:18
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface RcsMarketOddsConfigMapper extends BaseMapper<RcsMarketOddsConfig> {

	List<RcsMarketOddsConfig> queryListByTime(Map<String, Object> map);

	/**
	 * 根据盘口查询投注项期望值
	 * @param marketId
	 * @return
	 */
	List<OrderDetailStatReportVo> queryMarketStatByMarketId(@Param("marketId") Long marketId);

	/**
	 * @Description   添加记录或者更新记录
	 * @Param [rcsMarketOddsConfig]
	 * @Author  toney
	 * @Date  19:57 2020/2/18
	 * @return com.panda.sport.rcs.pojo.RcsMarketOddsConfig
	 **/
	int insertOrUpdate(RcsMarketOddsConfig rcsMarketOddsConfig);

	/**
	 * 批量更新
	 * @param list
	 * @return
	 */
	int batchInsertOrUpdate(@Param("list")List<RcsMarketOddsConfig> list);

	List<RcsMarketOddsConfig> queryBetNums(@Param("matchId") Long matchId);

	/**
	 * 查询盘口货量
	 * @param matchIds
	 * @return
	 */
	List<RcsMarketOddsConfig> queryMathBetNums(@Param("matchIds") List<Long> matchIds);
}