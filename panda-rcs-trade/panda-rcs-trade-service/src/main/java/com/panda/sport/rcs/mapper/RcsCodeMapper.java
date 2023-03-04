package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsCode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
public interface RcsCodeMapper extends BaseMapper<RcsCode> {

    @Select("SELECT father_key ,child_key,`value` from rcs_code WHERE father_key = 'playDisplay'")
    List<Map<String,String>> getPenSellConfig();

    List<String> selectPlayIdsByList(@Param("stateIds") List<Long> stateIds);

    List<Long> selectMerchantList();
}
