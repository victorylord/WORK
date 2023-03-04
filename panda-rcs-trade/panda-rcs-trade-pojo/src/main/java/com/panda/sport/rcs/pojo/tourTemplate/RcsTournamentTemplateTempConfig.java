package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.pojo.vo.ThirdDataSourceCodeVo;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class RcsTournamentTemplateTempConfig implements Serializable {
    /**
     * T常规
     */
    private Integer normal;
}
