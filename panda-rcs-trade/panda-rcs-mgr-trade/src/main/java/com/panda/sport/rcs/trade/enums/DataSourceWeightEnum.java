package com.panda.sport.rcs.trade.enums;

/**
 * 开售设置权重优先级
 */
public enum DataSourceWeightEnum {
    SR(1, "{\"SR\":1,\"BC\":2,\"BG\":3,\"TX\":4,\"RB\":5,\"AO\":6,\"PI\":7,\"PD\":8,\"LS\":9}"),
    BC(1, "{\"BG\":1,\"SR\":2,\"BC\":3,\"TX\":4,\"RB\":5,\"AO\":6,\"PI\":7,\"PD\":8,\"LS\":9}"),
    BG(1, "{\"BG\":1,\"SR\":2,\"BC\":3,\"TX\":4,\"RB\":5,\"AO\":6,\"PI\":7,\"PD\":8,\"LS\":9}"),
    TX(1, "{\"TX\":1,\"BG\":2,\"SR\":3,\"BC\":4,\"RB\":5,\"AO\":6,\"PI\":7,\"PD\":8,\"LS\":9}"),
    RB(1, "{\"RB\":1,\"SR\":2,\"BC\":3,\"BG\":4,\"TX\":5,\"AO\":6,\"PI\":7,\"PD\":8,\"LS\":9}"),
    AO(1, "{\"AO\":1,\"SR\":2,\"BC\":3,\"BG\":4,\"TX\":5,\"RB\":6,\"PI\":7,\"PD\":8,\"LS\":9}"),
    PI(1, "{\"PI\":1,\"SR\":2,\"BC\":3,\"BG\":4,\"TX\":5,\"RB\":6,\"AO\":7,\"PD\":8,\"LS\":9}"),
    PD(1, "{\"PD\":1,\"SR\":2,\"BC\":3,\"BG\":4,\"TX\":5,\"RB\":6,\"AO\":7,\"PI\":8,\"LS\":9}"),
    LS(1, "{\"LS\":1,\"SR\":2,\"BC\":3,\"BG\":4,\"TX\":5,\"RB\":6,\"AO\":7,\"PI\":8,\"PD\":9}");

    private Integer id;
    private String name;

    DataSourceWeightEnum(Integer id, String name) {
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
