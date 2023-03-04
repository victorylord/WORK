package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description  :  TODO
 * @author       :  Administrator
 * @Date:  2019-11-22 15:04
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface RcsBusinessSingleBetConfigMapper extends BaseMapper<RcsBusinessSingleBetConfig> {

    List<Integer> selectListTournamentLevels(@Param("config") RcsBusinessSingleBetConfig config);
}