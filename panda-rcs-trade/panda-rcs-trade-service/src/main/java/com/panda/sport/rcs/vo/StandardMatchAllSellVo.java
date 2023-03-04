package com.panda.sport.rcs.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.panda.sport.rcs.mongo.MarketCategory;
import lombok.Data;

import java.util.List;

/**
 * 操盘手确认开售页面,数据返回对象
 *
 * @author carver
 */
@Data
public class StandardMatchAllSellVo {


    /**
     * 体育种类ID
     */
    private Long sportId;

    /**
     * 标准赛事ID
     */
    private Long matchInfoId;

    private String beginTime;
    
    private String matchManageId;
    
    private Integer preMatchBusiness;
    
    private Integer liveOddBusiness;
    
    private String preRiskManagerCode;
    
    private String liveRiskManagerCode;
    
    private Integer operateMatchStatus;
    
    private Integer matchStatus;
    
    private String homeNameCode;

    private String awayNameCode;
    
    private String tournamentCode;
    
    private List<I18nItemVo> homeNameList;

    private List<I18nItemVo> awayNameList;
    
    private List<I18nItemVo> tournamentList;

}
