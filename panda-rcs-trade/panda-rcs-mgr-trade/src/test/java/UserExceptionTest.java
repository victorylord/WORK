import com.alibaba.fastjson.JSONObject;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.pojo.dto.UserExceptionDTO;
import com.panda.sport.rcs.pojo.dto.UserHistoryReqVo;
import com.panda.sport.rcs.trade.Bootstrap;
import com.panda.sport.rcs.trade.service.api.UserLimitApiService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.RcsUserSpecialBetLimitConfigsVo;
import com.panda.sport.rcs.trade.vo.SportIdVo;
import com.panda.sport.rcs.trade.wrapper.RcsUserConfigService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsOperationLogHistory;
import com.panda.sport.rcs.vo.RcsUserException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  PACKAGE_NAME
 * @Description :  TODO
 * @Date: 2020-02-10 21:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
@Slf4j
public class UserExceptionTest {
    @Autowired
    private UserLimitApiService userLimitApiService;
    @Autowired
    private RcsOperationLogMapper rcsOperationLogMapper;


    @Autowired
    private RcsUserConfigService rcsUserConfigService;
    @Test
    public void test22(){
        List<SportIdVo> sportIdVoList = CommonUtil.setBusiness();
        System.out.println(sportIdVoList);
    }
    @Test
    public void test(){
        log.info("queryUserExceptionByOnline：测试代码");
        UserExceptionDTO userExceptionDTO=new UserExceptionDTO();
        userExceptionDTO.setPageSize(10);
        userExceptionDTO.setPageNum(0);
        long endTime=System.currentTimeMillis();
        long startTime =endTime-604974668l;
        userExceptionDTO.setMerchantCode("497938272729300031");
        userExceptionDTO.setStartTime(startTime);
        userExceptionDTO.setEndTime(endTime);
        userExceptionDTO.setStartTime(1659628800000l);
        userExceptionDTO.setEndTime(1659931552000l);
        try {
            Response rs = userLimitApiService.queryUserExceptionByOnline(userExceptionDTO);
            System.out.println(rs);
            System.out.println("+++++++++++++++++++++++++++++++++++");
            System.out.println(rs);
        } catch (RcsServiceException ex) {
            System.out.println(ex.getErrorMassage());
        }
    }
    @Test
    public void UserOnLine(){
        log.info("queryUserExceptionByOnline：测试代码");
        UserHistoryReqVo userExceptionDTO=new UserHistoryReqVo();
        userExceptionDTO.setPageNum(0);
        userExceptionDTO.setPageSize(30);
        userExceptionDTO.setStartTime(1660233600000l);
        userExceptionDTO.setEndTime(1660885818000l);
        //userExceptionDTO.setType("3-4-5");

        if (StringUtils.isNotBlank(userExceptionDTO.getType())) {
            userExceptionDTO.setTypes(Arrays.asList(userExceptionDTO.getType().split("-")));
        }
        try {
            List<RcsOperationLogHistory> rs = rcsOperationLogMapper.selectRcsOperationLogByUserLimit(userExceptionDTO);
            System.out.println(rs);
            System.out.println("+++++++++++++++++++++++++++++++++++");
            System.out.println(rs);
        } catch (RcsServiceException ex) {
            System.out.println(ex.getErrorMassage());
        }
    }
    @Test
    public void UserOnLineUser(){
        String user = null;
        String type=null;
        Integer  total=1000;
        String likeUser = null;
        if (user != null) {
            likeUser = "%" + user + "%";
        }
        Long startTime=1660233600000l;
        Long endTime =1660885818000l;
        List<String> types = null;
        if (type != null && !type.equals("")) {
            types = Arrays.asList(type.split("-"));
        }
        try {
            List<RcsOperationLogHistory> rs = rcsOperationLogMapper.selectRcsOperationLogToatlByUser(user, types, startTime,endTime, total, likeUser);
            System.out.println(rs);
            System.out.println("+++++++++++++++++++++++++++++++++++");
            System.out.println(rs);
        } catch (RcsServiceException ex) {
            System.out.println(ex.getErrorMassage());
        }
    }
    @Test
    public void test_Order(){
        log.info("queryUserExceptionByOnline：测试代码");
        UserExceptionDTO userExceptionDTO=new UserExceptionDTO();
        userExceptionDTO.setPageSize(10);
       userExceptionDTO.setPageNum(0);
       userExceptionDTO.setCategory(0);
        long endTime=System.currentTimeMillis();
        long startTime =endTime-604974668l;

        userExceptionDTO.setStartTime(startTime);
        userExceptionDTO.setEndTime(endTime);
        userExceptionDTO.setUser("497833581740100031");
        List<RcsUserException> rcsUserExceptionList=rcsOperationLogMapper.selectRcsOperationLogByGroup(userExceptionDTO);
        rcsUserExceptionList.forEach(t->{
            String orgValue=t.getMerchantCode()+"_";
           Pattern p=Pattern.compile(orgValue);
            Matcher m=p.matcher(t.getUserName());
            String userName=m.replaceAll("").trim();
            t.setUserName(userName.trim());
            if(0==userExceptionDTO.getCategory() && StringUtils.isNotEmpty((t.getCrtTime()))){
                String sDate=t.getCrtTime().substring(0,10);
                t.setCrtTime(sDate);
            }
        });
        Integer total=rcsOperationLogMapper.selectRcsOperationLogByGroupCount(userExceptionDTO);
        System.out.println(total);
        List<RcsUserException> subList=rcsOperationLogMapper.selectRcsOperationLogByList(userExceptionDTO);
        rcsUserExceptionList.forEach(item->{
           List<RcsUserException> rcsUserExceptions = subList.stream().filter(t->t.getUid().equals(item.getUid()) && t.getCrtTime().substring(0,10).equals(item.getCrtTime())).collect(Collectors.toList());
           for (RcsUserException rcsUserException : rcsUserExceptions){
               if(rcsUserException.getId().equals(item.getId())){
                   continue;
               }
               if (item.getUpdateContents() == null) {
                   item.setUpdateContents(new ArrayList<>());
               }
               item.getUpdateContents().add(rcsUserException.getUpdateContent());
           }
        });
        System.out.println("++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(rcsUserExceptionList);
//        if(0==userExceptionDTO.getCategory()){
//            List<RcsUserException> rcsUserExceptionListNew=new ArrayList<>();
//            Map<String,List<RcsUserException>> mapUser= rcsUserExceptionList.stream().collect(Collectors.groupingBy(item->item.getUid()));
//            Set<Map.Entry<String, List<RcsUserException>>> setUsers=mapUser.entrySet();
//            for (Map.Entry<String, List<RcsUserException>> entry : setUsers){
//                List<RcsUserException> rcsUserExceptionLst=entry.getValue();
//                rcsUserExceptionLst.forEach(item->{
//                    RcsUserException rcsUserException= rcsUserExceptionListNew.stream().filter(t->t.getUid().equals(item.getUid()) && t.getCrtTime().equals(item.getCrtTime())).findFirst().orElse(null);
//                    if(Objects.isNull(rcsUserException)) {
//                        rcsUserExceptionListNew.add(item);
//                    }else{
//                        List<String> list=new ArrayList<>();
//                        list.add(item.getUpdateContent());
//                        rcsUserException.setUpdateContents(list);
//                    }
//                });
//            }
//            System.out.println("开始+++++++++++++++++++++++++++++++++++++++++++++++++");
//            System.out.println(rcsUserExceptionListNew);
//            System.out.println("==================================================");
//        }
    }

    @Test
    public void UserOptLog(){
        log.info("UserOptLog：测试代码");
        String content="{\n" +
                "  \"rcsUserConfigVo\": {\n" +
                "    \"userId\": \"502134374115300022\",\n" +
                "    \"sportIdList\": [\n" +
                "      1,\n" +
                "      2,\n" +
                "      3,\n" +
                "      4,\n" +
                "      5,\n" +
                "      6,\n" +
                "      7,\n" +
                "      8,\n" +
                "      9,\n" +
                "      10,\n" +
                "      11,\n" +
                "      12,\n" +
                "      13,\n" +
                "      14,\n" +
                "      15,\n" +
                "      16,\n" +
                "      17,\n" +
                "      18,\n" +
                "      19,\n" +
                "      21,\n" +
                "      22,\n" +
                "      23,\n" +
                "      24,\n" +
                "      25,\n" +
                "      26,\n" +
                "      27,\n" +
                "      28,\n" +
                "      29,\n" +
                "      30,\n" +
                "      31,\n" +
                "      32,\n" +
                "      33,\n" +
                "      34,\n" +
                "      35,\n" +
                "      36,\n" +
                "      37,\n" +
                "      38,\n" +
                "      39,\n" +
                "      40,\n" +
                "      41,\n" +
                "      50,\n" +
                "      100,\n" +
                "      101,\n" +
                "      103,\n" +
                "      104,\n" +
                "      341,\n" +
                "      1001,\n" +
                "      1002,\n" +
                "      1004,\n" +
                "      1007,\n" +
                "      1008,\n" +
                "      1009,\n" +
                "      1010,\n" +
                "      1011,\n" +
                "      1012\n" +
                "    ],\n" +
                "    \"betExtraDelay\": null,\n" +
                "    \"specialBettingLimit\": 4,\n" +
                "    \"remarks\": \"\",\n" +
                "    \"updateTime\": \"2023-01-16 12:55:34\",\n" +
                "    \"specialVolume\": null,\n" +
                "    \"settlementInAdvance\": 1,\n" +
                "    \"tagMarketLevelId\": \"0\",\n" +
                "    \"championLimitRate\": \"\"\n" +
                "  },\n" +
                "  \"rcsUserSpecialBetLimitConfigDataVoList\": [\n" +
                "    {\n" +
                "      \"specialBettingLimitType\": 4,\n" +
                "      \"rcsUserSpecialBetLimitConfigList1\": [\n" +
                "        {\n" +
                "          \"globalId\": null,\n" +
                "          \"id\": \"238096508151868731\",\n" +
                "          \"userId\": \"502134374115300022\",\n" +
                "          \"orderType\": 1,\n" +
                "          \"sportId\": -1,\n" +
                "          \"singleNoteClaimLimit\": 1000,\n" +
                "          \"oldSingleNoteClaimLimit\": 1000,\n" +
                "          \"singleNoteClaimLimitMax\": null,\n" +
                "          \"singleGameClaimLimit\": null,\n" +
                "          \"oldSingleGameClaimLimit\": null,\n" +
                "          \"singleGameClaimLimitMax\": null,\n" +
                "          \"status\": 1,\n" +
                "          \"specialBettingLimitType\": 4,\n" +
                "          \"percentageLimit\": null,\n" +
                "          \"oldPercentageLimit\": null,\n" +
                "          \"updateTime\": 1673844931000\n" +
                "        },\n" +
                "        {\n" +
                "          \"globalId\": null,\n" +
                "          \"id\": \"238096508151868732\",\n" +
                "          \"userId\": \"502134374115300022\",\n" +
                "          \"orderType\": 1,\n" +
                "          \"sportId\": 1,\n" +
                "          \"singleNoteClaimLimit\": 2000,\n" +
                "          \"oldSingleNoteClaimLimit\": 2000,\n" +
                "          \"singleNoteClaimLimitMax\": null,\n" +
                "          \"singleGameClaimLimit\": null,\n" +
                "          \"oldSingleGameClaimLimit\": null,\n" +
                "          \"singleGameClaimLimitMax\": null,\n" +
                "          \"status\": 0,\n" +
                "          \"specialBettingLimitType\": 4,\n" +
                "          \"percentageLimit\": null,\n" +
                "          \"oldPercentageLimit\": null,\n" +
                "          \"updateTime\": 1673844906000\n" +
                "        },\n" +
                "        {\n" +
                "          \"globalId\": null,\n" +
                "          \"id\": \"238096508151868733\",\n" +
                "          \"userId\": \"502134374115300022\",\n" +
                "          \"orderType\": 1,\n" +
                "          \"sportId\": 2,\n" +
                "          \"singleNoteClaimLimit\": 3000,\n" +
                "          \"oldSingleNoteClaimLimit\": 3000,\n" +
                "          \"singleNoteClaimLimitMax\": null,\n" +
                "          \"singleGameClaimLimit\": null,\n" +
                "          \"oldSingleGameClaimLimit\": null,\n" +
                "          \"singleGameClaimLimitMax\": null,\n" +
                "          \"status\": 0,\n" +
                "          \"specialBettingLimitType\": 4,\n" +
                "          \"percentageLimit\": null,\n" +
                "          \"oldPercentageLimit\": null,\n" +
                "          \"updateTime\": 1673844906000\n" +
                "        },\n" +
                "        {\n" +
                "          \"globalId\": null,\n" +
                "          \"id\": \"238096508151868734\",\n" +
                "          \"userId\": \"502134374115300022\",\n" +
                "          \"orderType\": 1,\n" +
                "          \"sportId\": 0,\n" +
                "          \"singleNoteClaimLimit\": 4000,\n" +
                "          \"oldSingleNoteClaimLimit\": 4000,\n" +
                "          \"singleNoteClaimLimitMax\": null,\n" +
                "          \"singleGameClaimLimit\": null,\n" +
                "          \"oldSingleGameClaimLimit\": null,\n" +
                "          \"singleGameClaimLimitMax\": null,\n" +
                "          \"status\": 0,\n" +
                "          \"specialBettingLimitType\": 4,\n" +
                "          \"percentageLimit\": null,\n" +
                "          \"oldPercentageLimit\": null,\n" +
                "          \"updateTime\": 1673844906000\n" +
                "        }\n" +
                "      ],\n" +
                "      \"rcsUserSpecialBetLimitConfigList2\": [\n" +
                "        {\n" +
                "          \"globalId\": null,\n" +
                "          \"id\": \"238096508151868735\",\n" +
                "          \"userId\": \"502134374115300022\",\n" +
                "          \"orderType\": 2,\n" +
                "          \"sportId\": -1,\n" +
                "          \"singleNoteClaimLimit\": 8000,\n" +
                "          \"oldSingleNoteClaimLimit\": 8000,\n" +
                "          \"singleNoteClaimLimitMax\": null,\n" +
                "          \"singleGameClaimLimit\": 2000,\n" +
                "          \"oldSingleGameClaimLimit\": 7000,\n" +
                "          \"singleGameClaimLimitMax\": null,\n" +
                "          \"status\": 1,\n" +
                "          \"specialBettingLimitType\": 4,\n" +
                "          \"percentageLimit\": null,\n" +
                "          \"oldPercentageLimit\": null,\n" +
                "          \"updateTime\": 1673844931000\n" +
                "        }\n" +
                "      ],\n" +
                "      \"validate\": true\n" +
                "    }\n" +
                "  ],\n" +
                "  \"userBetRateList\": [],\n" +
                "  \"updateType\": 2\n" +
                "}";

        RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo = JSONObject.parseObject(content,RcsUserSpecialBetLimitConfigsVo.class);

        try {
            Integer userId = 10020;
            HttpResponse response= rcsUserConfigService.updateRcsUserSpecialBetLimitConfigsVo(rcsUserSpecialBetLimitConfigsVo, userId, true);
            System.out.println(response);
            System.out.println("+++++++++++++++++++++++++++++++++++");
            System.out.println(response);
        } catch (RcsServiceException ex) {
            System.out.println(ex.getErrorMassage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
