package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.vo.SportMarketOddsQueryVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 赛事盘口交易项表 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface StandardSportMarketOddsMapper extends BaseMapper<StandardSportMarketOdds> {

    List<SportMarketOddsQueryVo> selectSportMarketOddsList(SportMarketOddsQueryVo condition,List<Long> marketCategoryIds);

	int updateMarketOddsInfo(StandardSportMarketOdds standardSportMarketOdds);

    int batchSaveOrUpdate(@Param("list") List<StandardSportMarketOdds> listStandardSportMarketOdds);
    /**
     * @Description   //根据位置获取盘口信息和赔率信息
     * @Param [marketConfig]
     * @Author  Sean
     * @Date  15:36 2020/10/3
     * @return com.panda.sport.rcs.pojo.StandardSportMarketOdds
     **/
    List<StandardSportMarketOdds> queryMarketInfoAndOdds(@Param("config") RcsMatchMarketConfig marketConfig);
    /**
     * @Description   //根据盘口查询赔率
     * @Param [config]
     * @Author  sean
     * @Date   2021/2/5
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarketOdds>
     **/
    RcsStandardMarketDTO queryMarketInfoAndOddsByMarketId(@Param("config")RcsMatchMarketConfig config);

    /**
     * 查询位置投注项信息
     *
     * @param matchId
     * @param playId
     * @param subPlayId
     * @param placeNum
     * @return
     */
    List<StandardSportMarketOdds> queryStandardSportMarketOdds(@Param("matchId") Long matchId, @Param("playId") Long playId, @Param("subPlayId") String subPlayId, @Param("placeNum") Integer placeNum);
}
