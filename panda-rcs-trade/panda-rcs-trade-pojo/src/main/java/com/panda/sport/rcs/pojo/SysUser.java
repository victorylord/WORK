package com.panda.sport.rcs.pojo;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

@Data
public class SysUser extends RcsBaseEntity<SysUser> {
    private Integer id;
    private String name;
    private String password;
}
