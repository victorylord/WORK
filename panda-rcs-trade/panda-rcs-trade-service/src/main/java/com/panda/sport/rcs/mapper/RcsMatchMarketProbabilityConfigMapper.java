package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketProbabilityConfig;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 赛事设置表 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-24
 */
@Component
public interface RcsMatchMarketProbabilityConfigMapper extends BaseMapper<RcsMatchMarketProbabilityConfig> {


    /**
     * @return void
     * @Description //更新概率差
     * @Param [ps]
     * @Author sean
     * @Date 2021/5/15
     **/
    int insertOrUpdateMarketProbabilityConfig(@Param("list") List<RcsMatchMarketProbabilityConfig> ps);

    /**
     * 根据参数更新水差为0P
     * @param list
     * @return
     */
    int updateProbabilityBySelectivetToZero(@Param("list") List<ClearSubDTO> list);
}
