package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * TournamentLevelTemporaryLog 日志
 * @author waldkir
 */
@Data
public class TournamentLevelTemporaryLog implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    /**
     * id
     */
    private Long id;

    /**
     * 联赛id
     */
    private Long tournamentId;

    /**
     * 接口入参
     */
    private String reqParam;

    @JsonSerialize(using = DateFormatSerializer.class)
    private Date createTime;
}