package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchConfig;
import com.panda.sport.rcs.vo.RcsMatchConfigVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description  :  TODO
 * @author       :  Administrator
 * @Date:  2019-11-08 20:18
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
*/
@Service
public interface RcsMatchConfigMapper extends BaseMapper<RcsMatchConfig> {
    List<RcsMatchConfig> selectMatchConfigByMatchIds(@Param("idList") List<Long> idList);

    List<RcsMatchConfigVo> selectMatchByMatchId(@Param("matchId") Integer matchId);

    List<RcsMatchConfigVo> selectMatchPlayByMatchId(@Param("matchId") Integer matchId);

    List<RcsMatchConfigVo> selectPlayByMatchId(@Param("matchId") Integer matchId);

    void updateOrInsert(@Param("matchId") Long matchId, @Param("priceAdjustmentParameters") BigDecimal priceAdjustmentParameters);

    void updateRiskManagerCode(Map<String,Object> map);
}