package com.panda.sport.rcs.trade.enums;

/**
 * 模板类型
 */
public enum TempTypeEnum {
    LEVEL(1, "级别"),
    TOUR(2, "联赛"),
    MATCH(3, "赛事");

    private Integer id;
    private String name;

    TempTypeEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
