package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 赛种
 * @Author : Paca
 * @Date : 2021-02-19 10:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum SportIdEnum {
    // 避免返回null
    DEFAULT(0L, "默认", Lists.newArrayList()),

    FOOTBALL(1L, "足球",Lists.newArrayList()),
    BASKETBALL(2L, "篮球",Lists.newArrayList()),
    BASEBALL(3L, "棒球",Lists.newArrayList(242L,273L,275L,277L,283L,243L,244L,245L,246L,247L,248L,249L,250L,251L,252L,274L,276L,278L,279L,280L,281L,282L,284L,285L,286L,287L,288L,289L,290L,291L,292L)),
    ICE_HOCKEY(4L, "冰球",Lists.newArrayList(1L,6L,28L,149L,3L,259L,261L,14L,8L,9L,204L,260L,265L,267L,296L,297L,298L,4L,2L,5L,257L,258L,15L,12L,262L,263L,264L,266L,268L,41L,294L,295L)),
    TENNIS(5L, "网球",Lists.newArrayList()),
    AMERICAN_FOOTBALL(6L, "美式足球",Lists.newArrayList(17L,37L,44L,50L,56L,62L,213L,19L,18L,42L,41L,39L,38L,198L,199L,40L,87L,97L,45L,51L,57L,63L,46L,52L,58L,64L,305L)),
    SNOOKER(7L, "斯洛克",Lists.newArrayList(1L,153L,184L,204L,190L,197L,180L,181L,182L,183L,185L,186L,187L,188L,189L,191L,192L,193L,194L,195L,196L)),
    PING_PONG(8L, "乒乓球",Lists.newArrayList()),
    VOLLEYBALL(9L, "排球",Lists.newArrayList(153L,159L,162L,204L,172L,173L,253L,254L,255L,256L)),
    BADMINTON(10L, "羽毛球",Lists.newArrayList(153L,175L,204L,174L,172L,173L,176L,177L,178L,179L,203L)),
    HANDBALL(11L, "手球",Lists.newArrayList(1L,6L,17L,70L,259L,4L,2L,19L,18L,5L,15L,43L,42L,127L,128L)),
    BOXING(12L, "拳击",Lists.newArrayList(153L,2L,337L,338L,339L)),
    BEACH_VOLLEYBALL(13L, "沙滩排球",Lists.newArrayList(153L,159L,162L,204L,172L,173L,253L,254L,255L,256L)),
    RUGBY_UNION(14L, "联合式橄榄球",Lists.newArrayList(1L,3L,6L,141L,17L,69L,70L,126L,218L,135L,136L,2L,4L,5L,10L,11L,15L,16L,18L,19L,42L,43L,87L,97L)),
    HOCKEY(15L, "曲棍球",Lists.newArrayList(1L,17L,6L,12L,3L,104L,70L,69L,24L,48L,44L,54L,50L,60L,56L,66L,62L,4L,2L,19L,18L,28L,5L,10L,11L,15L,81L,79L,43L,87L,97L,42L,46L,45L,47L,52L,51L,53L,58L,57L,59L,64L,63L,65L,145L,146L,223L,213L)),
    WATER_POLO(16L, "水球",Lists.newArrayList(1L,259L,17L,44L,50L,56L,62L,4L,2L,19L,46L,45L,47L,52L,51L,53L,58L,57L,59L,64L,63L,65L));

    private Long id;
    private String name;
    private List<Long> plays;
    
    public static boolean isIceHockey(Long sportId) {
        return ICE_HOCKEY.isYes(sportId);
    }
    
    public boolean isYes(Long sportId) {
        return this.getId().equals(sportId);
    }

    public boolean isYes(Integer sportId) {
        return sportId != null && this.getId().intValue() == sportId;
    }

    public boolean isNo(Long sportId) {
        return !this.isYes(sportId);
    }

    public boolean isNo(Integer sportId) {
        return !this.isYes(sportId);
    }

    public static List<Long> otherSports() {
        return Arrays.asList(3L,4L,5L,7L,8L,9L,10L);
    }

    public static boolean isFootball(Long sportId) {
        return FOOTBALL.isYes(sportId);
    }

    @Deprecated
    public static boolean isFootball(Integer sportId) {
        return FOOTBALL.isYes(sportId);
    }


    public static boolean isBasketball(Long sportId) {
        return BASKETBALL.isYes(sportId);
    }

    @Deprecated
    public static boolean isBasketball(Integer sportId) {
        return BASKETBALL.isYes(sportId);
    }


    public static boolean isTennis(Long sportId) {
        return TENNIS.isYes(sportId);
    }

    @Deprecated
    public static boolean isTennis(Integer sportId) {
        return TENNIS.isYes(sportId);
    }


    public static boolean isSnooker(Long sportId) {
        return SNOOKER.isYes(sportId);
    }

    public static SportIdEnum getBySportId(Long sportId) {
        for (SportIdEnum sportIdEnum : values()) {
            if (sportIdEnum.getId().equals(sportId)) {
                return sportIdEnum;
            }
        }
        return DEFAULT;
    }

    public static boolean isPingpong(Long sportId) {
        return PING_PONG.isYes(sportId);
    }

    public static boolean isVolleyball(Long sportId) {
        return VOLLEYBALL.isYes(sportId);
    }

    public static boolean isBaseBall(Long sportId) {
        return BASEBALL.isYes(sportId);
    }

    @Deprecated
    public static boolean isHandball(Integer sportId) {
        return HANDBALL.isYes(sportId);
    }

    @Deprecated
    public static boolean isSnooker(Integer sportId) {
        return SNOOKER.isYes(sportId);
    }

    @Deprecated
    public static boolean isBadminton(Integer sportId) {
        return BADMINTON.isYes(sportId);
    }

    @Deprecated
    public static boolean isBeachVolleyball(Integer sportId) {
        return BEACH_VOLLEYBALL.isYes(sportId);
    }

    @Deprecated
    public static boolean isBoxing(Integer sportId) {
        return BOXING.isYes(sportId);
    }

    @Deprecated
    public static boolean isRugbyUnion(Integer sportId) {
        return RUGBY_UNION.isYes(sportId);
    }

    public static List<Long> getAllPlaysBySportId(Long sportId) {
        return getBySportId(sportId).getPlays();
    }

    //判断非主玩法
    public static boolean noMain(Long sportId,Long setId){
        if (sportId == null || setId == null) {
            return false;
        }
        Long cateSetId = sportId * 10000 + 1L;
        return !setId.equals(cateSetId);
    }
}
