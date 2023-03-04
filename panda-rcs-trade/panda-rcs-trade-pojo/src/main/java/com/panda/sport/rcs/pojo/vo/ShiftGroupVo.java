package com.panda.sport.rcs.pojo.vo;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.List;

@Data
public class ShiftGroupVo extends RcsBaseEntity<ShiftGroupVo> {

    private String title;

    private String id;

    private List children;

    @Data
    public class ShiftGroupSubVo {
        private String title;

        private String id;

        private List children;
    }

}
