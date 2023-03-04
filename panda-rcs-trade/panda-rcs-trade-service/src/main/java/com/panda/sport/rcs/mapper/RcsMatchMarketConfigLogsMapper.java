package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigLogs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.console.dao
 * @Description :  TODO
 * @Date: 2020-02-10 15:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public interface RcsMatchMarketConfigLogsMapper extends BaseMapper<RcsMatchMarketConfigLogs> {
    /**
     * @return void
     * @Description //玩法配置 需要记录进日志数据库
     * @Param [ids, rcsMatchPlayConfig]
     * @Author kimi
     * @Date 2020/2/10
     **/
    void insertRcsMatchMarketConfigLogs(@Param("ids") List<Long> ids, @Param("matchId") Long matchId, @Param("status") Integer status, @Param("dataSource") Integer dataSource,
                                        @Param("changeLevel") Integer changeLevel);
}
