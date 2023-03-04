package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.StandardSportType;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 标准体育种类表.  Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-26
 */
@Service
public interface StandardSportTypeMapper extends BaseMapper<StandardSportType> {

    /**
     * @MethodName: getLastCrtTime
     * @Description: 得到最后的插入时间
     * @Param: []
     * @Return: java.lang.Long
     * @Author: Vector
     * @Date: 2019/9/28
     **/
    @Select("SELECT max(modify_time) from standard_sport_type")
    Long getLastCrtTime();

}
