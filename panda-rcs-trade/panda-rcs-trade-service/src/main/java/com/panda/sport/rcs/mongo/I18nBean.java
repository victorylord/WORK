package com.panda.sport.rcs.mongo;

import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.constants.Placeholder;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.factory.BeanFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Map;

import static com.panda.sport.rcs.constants.Placeholder.*;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :多语言类
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class I18nBean implements Serializable {

    private static final long serialVersionUID = -4950031624625460830L;

    /**
     * 中文简体
     */
    private String zs;

    /**
     * 英文
     */
    private String en;

    /**
     * 简称
     */
    private String jc;

    /**
     * 中文繁体
     */
    private String zh;

    public I18nBean(Map<String, String> names) {
        this.zs = names.get("zs");
        this.en = names.get("en");
        this.jc = names.get("jc");
        this.zh = names.get("zh");
    }

    /**
     * 占位符，{$competitor1}、{$competitor2}等
     *
     * @param placeholder
     */
    public I18nBean(String placeholder) {
        this.zs = placeholder;
        this.en = placeholder;
        this.jc = placeholder;
        this.zh = placeholder;
    }

    public void replace(String placeholder, String target) {
        this.zs = StringUtils.trim(StringUtils.replace(this.zs, placeholder, target));
        this.en = StringUtils.trim(StringUtils.replace(this.en, placeholder, target));
        this.jc = StringUtils.trim(StringUtils.replace(this.jc, placeholder, target));
        this.zh = StringUtils.trim(StringUtils.replace(this.zh, placeholder, target));
    }

    public void replaceTeam(Map<String, I18nBean> teamMap) {
        if (CollectionUtils.isEmpty(teamMap)) {
            teamMap.put(RcsConstant.HOME_POSITION, BeanFactory.getHomeTeam());
            teamMap.put(RcsConstant.AWAY_POSITION, BeanFactory.getAwayTeam());
        }
        I18nBean homeTeam = teamMap.get(RcsConstant.HOME_POSITION);
        if (homeTeam == null) {
            homeTeam = BeanFactory.getHomeTeam();
        }
        I18nBean awayTeam = teamMap.get(RcsConstant.AWAY_POSITION);
        if (awayTeam == null) {
            awayTeam = BeanFactory.getAwayTeam();
        }
        replace(Placeholder.HOME, homeTeam);
        replace(Placeholder.AWAY, awayTeam);
        replace(Placeholder.HOME2, homeTeam);
        replace(Placeholder.AWAY2, awayTeam);
    }

    public void replaceOverAndUnder() {
        replace(Placeholder.OVER, BeanFactory.getOverI18n());
        replace(Placeholder.UNDER, BeanFactory.getUnderI18n());
    }

    public void replaceTitle(Map<String, I18nBean> teamMap) {
        replace(Placeholder.DRAW, BeanFactory.getDrawI18n());
        replace(Placeholder.DRAW0, BeanFactory.getNoneI18n());//draw0=无进球，直接使用none国际化
        replace(Placeholder.DRAW1, BeanFactory.getDraw1I18n());
        replace(Placeholder.OTHER, BeanFactory.getOtherI18n());
        replace(Placeholder.DRAW_OTHER, BeanFactory.getDarwOtherI18n());
        replaceOverAndUnder();
        replaceTeam(teamMap);
    }

    public void replaceGoal(String replacement) {
        replace(Placeholder.WHICH_GOAL, replacement);
        replace(Placeholder.WHICH_GOAL2, replacement);
    }

    public void replaceCorner(String replacement) {
        replace(Placeholder.WHICH_CORNER, replacement);
        replace(Placeholder.WHICH_CORNER2, replacement);
    }

    public void replaceScore(String replacement) {
        replace(Placeholder.WHICH_SCORE, replacement);
        replace(Placeholder.WHICH_SCORE2, replacement);
    }

    public void replaceBooking(String replacement) {
        replace(Placeholder.WHICH_BOOKING, replacement);
        replace(Placeholder.WHICH_BOOKING2, replacement);
    }

    public void replacePenalty(String replacement) {
        replace(Placeholder.WHICH_PENALTY, replacement);
        replace(Placeholder.WHICH_PENALTY2, replacement);
    }

    public void replaceQuarter(String replacement) {
        replace(Placeholder.WHICH_QUARTER, replacement);
        replace(Placeholder.WHICH_QUARTER2, replacement);
    }

    public void replaceInningnr(String replacement){
        replace(Placeholder.INNINGNR, replacement);
        replace(Placeholder.INNINGNR2, replacement);
    }

    public void replaceInningnrX(String replacement){
        replace(Placeholder.WHICH_GOAL_x, replacement);
        replace(Placeholder.WHICH_GOAL_CHINESE_x, replacement);
    }

    public void replaceSet(String replacement) {
        replace(Placeholder.WHICH_SET, replacement);
        replace(Placeholder.WHICH_SET2, replacement);
    }

    public void replaceGame(String replacement) {
        replace(Placeholder.WHICH_GAME, replacement);
        replace(Placeholder.WHICH_GAME2, replacement);
    }

    public void replacePoint(String replacement) {
        replace(Placeholder.POINT, replacement);
        replace(Placeholder.POINT2, replacement);
    }

    public void replaceFrame(String replacement) {
        replace(Placeholder.WHICH_FRAME, replacement);
        replace(Placeholder.WHICH_FRAME2, replacement);
    }

    public void replaceXth(String replacement) {
        replace(Placeholder.WHICH_XTH, replacement);
        replace(Placeholder.WHICH_XTH2, replacement);
    }

    public void replacePeriod(String replacement) {
        replace(Placeholder.WHICH_PERIOD, replacement);
        replace(Placeholder.WHICH_PERIOD2, replacement);
        replace(Placeholder.WHICH_PERIOD3, replacement);
    }

    public void replaceFromAndTo(String from, String to) {
    	if(NumberUtils.isNumber(from)) {
    		int fromNum = Double.valueOf(from).intValue();
    		fromNum = fromNum - 1;
    		from = String.format("%02d", fromNum) + ":00";
    	}
    	if(NumberUtils.isNumber(to)) {
    		int toNum = Double.valueOf(to).intValue();
    		toNum = toNum - 1;
    		to = String.format("%02d", toNum) + ":59";
    	}
    	
        replace(FROM, from);
        replace(Placeholder.TO, to);
    }
    
    public void replaceTotal(String total) {
        replace(Placeholder.TOTAL, total);
    }

    public void replaceWithA1(String addition1) {
        /*
         * A1 ->
         * {!goalnr}/{goalnr}
         * {total}
         * {!cornernr}/{cornernr}
         * {!penaltynr}/{penaltynr}
         * {!quarternr}/{quarternr}
         * {!pointnr}/{pointnr}
         * {!setnr}/{setnr}
         * {!scorenr}/{scorenr}
         * {!bookingnr}/{bookingnr}
         * {!periodnr}/{periodnr}/!periodnr}
         */
        replaceGoal(addition1);
        replaceTotal(addition1);
        replaceCorner(addition1);
        replacePenalty(addition1);
        replaceQuarter(addition1);
        replacePoint(addition1);
        replaceSet(addition1);
        replaceScore(addition1);
        replaceBooking(addition1);
        replacePeriod(addition1);
        replaceInningnr(addition1);
    }

    public void replaceWithA2(String addition2) {
        /*
         * A2 ->
         * {!quarternr}/{quarternr}
         * {!setnr}/{setnr}
         * {!gamenr}/{gamenr}
         * {!framenr}/{framenr}
         * {!periodnr}/{periodnr}/!periodnr}
         */
        replaceQuarter(addition2);
        replaceSet(addition2);
        replaceGame(addition2);
        replaceFrame(addition2);
        replacePeriod(addition2);
        replaceInningnr(addition2);
    }

    private void replace(String placeholder, I18nBean bean) {
        if (bean != null && StringUtils.contains(this.zs, placeholder)) {
            if (StringUtils.isNotBlank(bean.getZs())) {
                this.zs = StringUtils.trim(StringUtils.replace(this.zs, placeholder, bean.getZs()));
            } else if (StringUtils.isNotBlank(bean.getEn())) {
                this.zs = StringUtils.trim(StringUtils.replace(this.zs, placeholder, bean.getEn()));
            }
            this.en = StringUtils.trim(StringUtils.replace(this.en, placeholder, bean.getEn()));
            this.jc = StringUtils.trim(StringUtils.replace(this.jc, placeholder, bean.getJc()));
            this.zh = StringUtils.trim(StringUtils.replace(this.zh, placeholder, bean.getZh()));
        }
    }

    public void baseBallSectionReplace(String str,String strTwo){
        this.zs=StringUtils.trim(StringUtils.replace(StringUtils.replace(this.zs,A,str),B,strTwo));
        this.zh=StringUtils.trim(StringUtils.replace(StringUtils.replace(this.zh,A,str),B,strTwo));
        this.jc=StringUtils.trim(StringUtils.replace(StringUtils.replace(this.jc,A,str),B,strTwo));
        this.en=StringUtils.trim(StringUtils.replace(StringUtils.replace(this.en,FROM,str),TO,strTwo));
    }

    public static I18nBean getHome(Map<String, I18nBean> teamMap) {
        if (CollectionUtils.isEmpty(teamMap)) {
            return new I18nBean(Placeholder.HOME);
        }
        I18nBean bean = teamMap.getOrDefault(RcsConstant.HOME_POSITION, BeanFactory.getHomeTeam());
        if (StringUtils.isBlank(bean.getZs()) && StringUtils.isNotBlank(bean.getEn())) {
            bean.setZs(bean.getEn());
        }
        return bean;
    }
    public static I18nBean getAXB(String name, Map<String, I18nBean> teamMap) {
        if (CollectionUtils.isEmpty(teamMap)) {
            teamMap.put(RcsConstant.HOME_POSITION, BeanFactory.getHomeTeam());
            teamMap.put(RcsConstant.AWAY_POSITION, BeanFactory.getAwayTeam());
        }
        I18nBean homeTeam = teamMap.get(RcsConstant.HOME_POSITION);
        if (homeTeam == null) {
            homeTeam = BeanFactory.getHomeTeam();
        }
        I18nBean awayTeam = teamMap.get(RcsConstant.AWAY_POSITION);
        if (awayTeam == null) {
            awayTeam = BeanFactory.getAwayTeam();
        }
        I18nBean bean = new I18nBean(name);
        bean.replace(Placeholder.HOME, homeTeam);
        bean.replace(Placeholder.AWAY, awayTeam);
        bean.replace(Placeholder.DRAW, BeanFactory.getDrawI18n());
        return bean;
    }

    public static I18nBean getAway(Map<String, I18nBean> teamMap) {
        if (CollectionUtils.isEmpty(teamMap)) {
            return new I18nBean(Placeholder.AWAY);
        }
        I18nBean bean = teamMap.getOrDefault(RcsConstant.AWAY_POSITION, BeanFactory.getAwayTeam());
        if (StringUtils.isBlank(bean.getZs()) && StringUtils.isNotBlank(bean.getEn())) {
            bean.setZs(bean.getEn());
        }
        return bean;
    }

}
