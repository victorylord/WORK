package com.panda.sport.rcs.trade.wrapper;


import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsShift;
import com.panda.sport.rcs.pojo.dto.ShiftDto;
import com.panda.sport.rcs.pojo.vo.ShiftGroupVo;

import java.util.List;
import java.util.Map;

public interface RcsShiftService  extends IService<RcsShift> {

    /**
     * 排班数据
     * @param shiftDto
     * @return
     */
    Map shiftUserList(ShiftDto shiftDto);

    /**
     * 变理排班
     * @param shiftDto
     */
    void updateShiftList(ShiftDto shiftDto);

    /**
     * 操盘手选择树
     * @param shiftDto
     * @param lang
     * @return
     */
    List<ShiftGroupVo> shiftUserGroupList(ShiftDto shiftDto, String lang);
}


