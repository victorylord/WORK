package com.panda.sport.rcs.vo;

/**
 * @author :  max
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.rpc.paid.matrix
 * @Description :  TODO
 * @Date: 2019-10-04 20:22
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class Category {
    /**
     * @Description   玩法ID
     **/
    private Integer id;
    /**
     * @Description   玩法编码
     **/
    private String  code;
    /**
     * @Description   玩法英文名
     **/
    private String nameEn;
    /**
     * @Description   玩法中文名
     **/
    private String nameZh;
    /**
     * @Description   玩法矩阵类型
     * 0 矩阵计算
     * 1 穷举计算
     **/
    private Integer ctype;
    /**
     * @Description  玩法是否需要基准分
     * 0 不需要
     * 1 需要基准分
     **/
    private Integer benchmark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public Integer getCtype() {
        return ctype;
    }

    public void setCtype(Integer ctype) {
        this.ctype = ctype;
    }

	public Integer getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(Integer benchmark) {
		this.benchmark = benchmark;
	}
    
}
