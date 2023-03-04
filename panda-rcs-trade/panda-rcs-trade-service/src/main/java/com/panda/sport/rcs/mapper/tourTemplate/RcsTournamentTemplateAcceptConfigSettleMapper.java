package com.panda.sport.rcs.mapper.tourTemplate;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfigSettle;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @Description  足球滚球 联赛模板  接拒单结算  事件从表
 * @Param 
 * @Author  carver
 * @Date  20:02 2021/10/09
 * @return 
 **/
@Repository
public interface RcsTournamentTemplateAcceptConfigSettleMapper extends BaseMapper<RcsTournamentTemplateAcceptConfigSettle> {

    /**
     * @Description   //更新赛事数据源和时间配置
     * @Param [config]
     * @Author  Sean
     * @Date  15:40 2021/10/17
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    void updateMatchDataSourceAndTimeConfigSettle(@Param("config") RcsTournamentTemplateAcceptConfigSettle config);

	/**
	 *	根据模板id更新数据源 
	 * @param dataSourceCode
	 * @param templateId
	 * @return
	 */
	int updateMatchDataSourceByTemplateId(@Param("dataSourceCode") String dataSourceCode, @Param("templateId") Long templateId);
}