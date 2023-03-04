package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.vo.StandardMatchInfoVo;
import lombok.Data;

import java.util.Map;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-11-26 16:28
 **/
@Data
public class StandardMatchInfoBaseVo  {
    private  Integer matchSnapshot;
    private StandardMatchInfoVo standardMatchInfo;
    /**
     * 当前比分信息
     */
    private String score;
    /**
     * 比分
     */
    private Map<Integer,String> totalScore;
    /**
     * 对阵信息多语言
     */
    private Map<String,Map<String,String>> matchInfoLanguage;

    private String businessEvent;

}
