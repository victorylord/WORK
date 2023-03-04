package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class RcsMatchUserMemoRef implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     *备忘录记录Id
     */
    private String memoId;

    /**
     *标准赛事的id.对应standard_match_info.id
     */
    private Long standardMatchId;

    /**
     *操盘手Id
     */
    private String traderId;

    /**
     *是否已阅.0：未读1：已读
     */
    private Integer readStatus;

    /**
     *创建时间
     */
    private Long createTime;

    /**
     *更改时间
     */
    private Long updateTime;

    private static final long serialVersionUID = 1L;



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", memoId=").append(memoId);
        sb.append(", standardMatchId=").append(standardMatchId);
        sb.append(", traderId=").append(traderId);
        sb.append(", readStatus=").append(readStatus);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}