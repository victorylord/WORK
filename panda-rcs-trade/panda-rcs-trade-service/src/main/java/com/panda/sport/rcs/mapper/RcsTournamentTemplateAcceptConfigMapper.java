package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfigSettle;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 多语言 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface RcsTournamentTemplateAcceptConfigMapper extends BaseMapper<RcsTournamentTemplateAcceptConfig> {
    /**
     * @Description   //根据赛事查询接拒单时间和数据源配置
     * @Param [config]
     * @Author  Sean
     * @Date  11:26 2020/9/5
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    RcsTournamentTemplateAcceptConfig queryMatchDataSourceAndTimeConfig(@Param("config") RcsTournamentTemplateAcceptConfig config);
    /**
     * @Description   //更新赛事数据源和时间配置
     * @Param [config]
     * @Author  Sean
     * @Date  15:40 2020/9/5
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    void updateMatchDataSourceAndTimeConfig(@Param("config") RcsTournamentTemplateAcceptConfig config);
    /**
     * @Description   //根据赛事id和数据源编码，获取接拒配置
     * @Param [config]
     * @Author  carver
     * @Date  15:40 2021/3/10
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    List<RcsTournamentTemplateAcceptConfig> queryAcceptConfigByMatchId(@Param("standardMatchId") Long standardMatchId,@Param("dataSourceCode") String dataSourceCode);
    
    /**
     * @Description   //根据赛事查询接拒单时间和数据源配置
     * @Param [config]
     * @Author  Sean
     * @Date  11:26 2020/9/5
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    String queryConfigCode(@Param("sportId") Long sportId,@Param("matchId") Long matchId,@Param("categorySetId") Long categorySetId);
    
	/**
	 * 	根据模板id更新数据源 
	 * @param dataSourceCode
	 * @param id
	 * @return
	 */
	int updateMatchDataSourceByTemplateId(@Param("dataSourceCode") String dataSourceCode, @Param("templateId") Long templateId);

    /**
     * @Description   //根据赛事提前结算接拒数据源配置
     * @Param [config]
     * @Author  Sean
     * @Date  11:26 2020/9/5
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    String querySettleCode(@Param("sportId") Long sportId,@Param("matchId") Long matchId,@Param("categorySetId") Long categorySetId);
}
