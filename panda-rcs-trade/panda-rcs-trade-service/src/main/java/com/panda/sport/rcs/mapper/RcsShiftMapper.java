package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsShift;
import com.panda.sport.rcs.pojo.dto.ShiftDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public interface RcsShiftMapper extends BaseMapper<RcsShift> {
    int updateBatch(List<RcsShift> list);

    int batchInsert(@Param("list") List<RcsShift> list);

    int insertOrUpdate(RcsShift record);

    int insertOrUpdateSelective(RcsShift record);

    List<RcsShift> shiftUserList(@Param("bean") ShiftDto shiftDto,@Param("list") List<String> users);

    int batchInsertOrUpdate(@Param("list") List<RcsShift> list);

    List<RcsShift> noDesignateUserList(@Param("list") List<String> users);

    int deleteByUserCode(@Param("list") List<String> deleteShifts);
    /**
     * @Description   //根据用户id获取球种
     * @Param [userId]
     * @Author  sean
     * @Date   2022/6/10
     * @return java.lang.Integer
     **/
    Integer getShiftByUserId(@Param("userId") Integer userId);
}