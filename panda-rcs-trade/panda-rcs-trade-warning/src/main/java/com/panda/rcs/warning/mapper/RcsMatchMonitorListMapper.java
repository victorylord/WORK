package com.panda.rcs.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.rcs.warning.vo.RcsMatchMonitorList;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.mapper
 * @Description :  TODO
 * @Date: 2022-07-19 15:43
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsMatchMonitorListMapper extends BaseMapper<RcsMatchMonitorList> {
}
