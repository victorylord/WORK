package com.panda.rcs.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.rcs.warning.vo.RcsMatchMonitorMqLicense;
import org.springframework.stereotype.Repository;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.mapper
 * @Description :  TODO
 * @Date: 2022-07-26 15:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsMatchMonitorMqLicenseMapper extends BaseMapper<RcsMatchMonitorMqLicense> {
      void insertOrUpdate(RcsMatchMonitorMqLicense rcsMatchMonitorMqLicense);
}
