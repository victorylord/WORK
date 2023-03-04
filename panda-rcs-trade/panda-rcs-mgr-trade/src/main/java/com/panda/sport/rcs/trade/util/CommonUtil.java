package com.panda.sport.rcs.trade.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.trade.vo.SportIdVo;
import com.panda.sport.rcs.trade.vo.dto.ThirdMatchDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.util
 * @Description :  TODO
 * @Date: 2022-03-06 21:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
public class CommonUtil {

    public static final String RCS_BUSINESS_LOG_SAVE = "rcs_business_log_save";

    public static final String Bet_Extra_Delay="投注延时-时间";

    public static final String Sport_ID="投注延时-赛种";

    public static final String Tag_Market_Level_Id="赔率分组";

    public static final String Special_Betting_Limit="投注限额";

    public static final String Limit_Percentage="投注限额-限额百分比";

    public static final String Extra_Margin="提前结算margin";

    public static final String Volume_Percentage_Sport_ID="货量百分比-赛种";


    public static final String Volume_Percentage="货量百分比";
    public static List<SportIdVo> setBusiness(){
        List<SportIdVo> sportIdVos =new ArrayList<>();
        SportIdVo sportIdVo =new SportIdVo();
        sportIdVo.setSportId(1);
        sportIdVo.setSportName("足球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(2);
        sportIdVo.setSportName("篮球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(3);
        sportIdVo.setSportName("棒球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(4);
        sportIdVo.setSportName("冰球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(5);
        sportIdVo.setSportName("网球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(6);
        sportIdVo.setSportName("美式足球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(7);
        sportIdVo.setSportName("斯诺克");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(8);
        sportIdVo.setSportName("乒乓球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(9);
        sportIdVo.setSportName("排球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(10);
        sportIdVo.setSportName("羽毛球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(11);
        sportIdVo.setSportName("手球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(12);
        sportIdVo.setSportName("拳击");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(13);
        sportIdVo.setSportName("沙滩排球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(14);
        sportIdVo.setSportName("联合式橄榄球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(15);
        sportIdVo.setSportName("曲棍球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(16);
        sportIdVo.setSportName("水球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(17);
        sportIdVo.setSportName("田径");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(18);
        sportIdVo.setSportName("4 x 10 km Relay");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(19);
        sportIdVo.setSportName("游泳");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(20);
        sportIdVo.setSportName("体操");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(21);
        sportIdVo.setSportName("跳水");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(22);
        sportIdVo.setSportName("射击");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(23);
        sportIdVo.setSportName("举重");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(24);
        sportIdVo.setSportName("射箭");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(25);
        sportIdVo.setSportName("击剑");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(26);
        sportIdVo.setSportName("冰壶");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(27);
        sportIdVo.setSportName("跆拳道");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(28);
        sportIdVo.setSportName("高尔夫");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(29);
        sportIdVo.setSportName("自行车");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(30);
        sportIdVo.setSportName("赛马");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(31);
        sportIdVo.setSportName("帆船");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(32);
        sportIdVo.setSportName("划船");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(33);
        sportIdVo.setSportName("赛车运动");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(34);
        sportIdVo.setSportName("柔道");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(35);
        sportIdVo.setSportName("空手道");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(36);
        sportIdVo.setSportName("摔跤");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(37);
        sportIdVo.setSportName("板球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(38);
        sportIdVo.setSportName("飞镖");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(39);
        sportIdVo.setSportName("沙滩足球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(40);
        sportIdVo.setSportName("其他");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(41);
        sportIdVo.setSportName("联盟式橄榄球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(50);
        sportIdVo.setSportName("趣味");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(100);
        sportIdVo.setSportName("英雄联盟");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(101);
        sportIdVo.setSportName("Dota2");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(102);
        sportIdVo.setSportName("CS:GO");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(103);
        sportIdVo.setSportName("王者荣耀");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(104);
        sportIdVo.setSportName("绝地求生");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1001);
        sportIdVo.setSportName("VR足球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1002);
        sportIdVo.setSportName("VR赛狗");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1004);
        sportIdVo.setSportName("VR篮球");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1007);
        sportIdVo.setSportName("VR泥地赛车");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1008);
        sportIdVo.setSportName("VR卡丁车");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1009);
        sportIdVo.setSportName("VR泥地摩托车");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1010);
        sportIdVo.setSportName("VR摩托车");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1011);
        sportIdVo.setSportName("VR赛马");
        sportIdVos.add(sportIdVo);

        sportIdVo=new SportIdVo();
        sportIdVo.setSportId(1012);
        sportIdVo.setSportName("VR马车赛");
        sportIdVos.add(sportIdVo);
        return  sportIdVos;
        //sportIdVos.stream().filter(t->t.getSportId().equals(value)).findFirst().orElse(null);
    }
    public static final String logCode="10030";
    public static String getId(String jsonArray, String dataSourceCode) {
        try {
            if (StringUtils.isBlank(jsonArray) || StringUtils.isBlank(dataSourceCode)) {
                return null;
            }
            String aoId = null;
            log.info("获取数据源ID信息:{},{}",jsonArray,dataSourceCode);
            List<ThirdMatchDTO> thirdMatchDTOS = JsonFormatUtils.fromJsonArray(jsonArray, ThirdMatchDTO.class);
            for (ThirdMatchDTO thirdMatchDTO : thirdMatchDTOS) {
                if (thirdMatchDTO.getDataSourceCode().equals(dataSourceCode)) {
                    aoId = thirdMatchDTO.getThirdMatchSourceId();
                }
            }
            return aoId;
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return null;
    }
    /**
     * 导出处理分页
     * */
    public  static  Integer getPageCount(int total,int pageSize){
        if (total > 50000){
            log.warn("Excel一次导出数量不可超过50000条");
            throw  new RuntimeException("Excel一次导出数量不可超过50000条");
        }
        int pageCount = total / pageSize + (total % pageSize > 0 ? 1 : 0);
        return pageCount == 0 ? 1 : pageCount;
    }
    public static String getDataSourceCode(String earlySettStr){
        if(StringUtils.isNotBlank(earlySettStr)){
            Map<String, Integer> sourceCodeMap = JSON.parseObject(earlySettStr, Map.class);
            for (Map.Entry<String, Integer> entry : sourceCodeMap.entrySet()) {
                if (entry.getValue() == 1) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }

    /**
     * 判断字符串 null 或 ‘null’
     * @param str
     * @return
     */
    public static boolean isBlankOrNull(String str){
        return StringUtils.isBlank(str) || str.equals("null");
    }

    /**
     * 获取requestId or 生成唯一id
     * @return
     */
    public static String getRequestId(){
        String key = "-";
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            key = request.getHeader("request-id");
            if (StringUtils.isBlank(key)) {
                key = UuidUtils.generateUuid();
            }
        }catch (NullPointerException e){
            key = UuidUtils.generateUuid();
        }catch (Exception e){
            key = UuidUtils.generateUuid();
        }
        return key;
    }

    /**
     * 使用自定义的requestId
     * @param requestId
     * @return
     */
    public static String getRequestId(Object... requestId){
        if(Objects.isNull(requestId) || requestId.length==0){
            return getRequestId();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : requestId) {
            if(!Objects.isNull(o)){
                stringBuilder.append(o).append("_");
            }
        }
        if(stringBuilder.length()==0){
            return getRequestId();
        }
        return stringBuilder.deleteCharAt(stringBuilder.length()-1).toString();
    }

    /**
     * 获取 linkId
     * @param message
     * @return
     */
    public static String getLinkId(String message) {
        try {
            String linkId = "";
            if (isBlankOrNull(message)) {
                return "";
            }
            JSONObject jsonObject = JSONObject.parseObject(message);
            linkId = jsonObject.getString("linkId");
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("globalId");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("matchId");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("matchInfoId");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("standardMatchInfoId");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("standardMatchId");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("msgId");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("matchid");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("matchinfoId");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("matchinfoid");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("standardmatchinfoid");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("linkid");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("globalid");
            }
            if (isBlankOrNull(linkId)) {
                linkId = jsonObject.getString("msgid");
            }
            if (isBlankOrNull(linkId)) {
                linkId = "";
            } else {
                linkId = "_" + linkId;
            }
            return linkId;
        } catch (JSONException e){
            log.info("getLinkId非Json:{}", message);
        } catch (Exception e) {
            log.error("getLinkId报错{},{},{}", message, e.getMessage(), e);
        }
        return "";
    }


}
