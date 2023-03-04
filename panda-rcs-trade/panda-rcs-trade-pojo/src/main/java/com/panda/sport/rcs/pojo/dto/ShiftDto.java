package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.pojo.RcsShift;
import lombok.Data;

import java.util.List;

@Data
public class ShiftDto extends RcsBaseEntity<ShiftDto> {

    private String userName;

    private List<RcsShift> shifts;

    private List<String> users;

    private Integer shift;

    private String sportId;
    /**
     * 用户id
     */
    private String userId;

}
