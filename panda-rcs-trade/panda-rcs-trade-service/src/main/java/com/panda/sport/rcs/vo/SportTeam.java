package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.StandardSportTeam;
import lombok.Data;

@Data
public class SportTeam extends StandardSportTeam {

    /**
     * 比赛中的作用。足球：主客队或者其他.home:主场队;away:客场队
     */
    private String matchPosition;

}
