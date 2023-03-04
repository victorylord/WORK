package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.StandardMatchTeamRelation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2019-11-19 16:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface StandardMatchTeamRelationMapper extends BaseMapper<StandardMatchTeamRelation> {
    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description //TODO
     * @Param [macthId]
     * @Author kimi
     * @Date 2019/11/19
     **/
    List<Map<String, Object>> selectByMatchId(Integer macthId);
}
