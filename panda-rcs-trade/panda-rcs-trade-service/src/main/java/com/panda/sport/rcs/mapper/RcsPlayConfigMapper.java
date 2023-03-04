package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsPlayConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2020-02-17 22:33
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public interface RcsPlayConfigMapper extends BaseMapper<RcsPlayConfig> {
    /**
     * @return void
     * @Description //TODO
     * @Param [rcsPlayConfig, playIdList]
     * @Author kimi
     * @Date 2020/2/18
     **/
    void updateOrInsertRcsPlayConfigList(@Param("matchId") Long matchId, @Param("dataSource") Integer dataSource, @Param("status") Integer status, @Param("playIdList") List<Long> playIdList);
}
