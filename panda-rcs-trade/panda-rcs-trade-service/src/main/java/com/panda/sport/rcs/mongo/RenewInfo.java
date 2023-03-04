package com.panda.sport.rcs.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  更新时间
 */
@Document(collection = "renew_info")
@Data
public class RenewInfo implements Serializable {

    @Field(value = "id")
    private Long id;

    private String tableName;

    private long updateTime;

}
