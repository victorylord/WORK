//package com.panda.sport.rcs.trade.service;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
//import com.panda.sport.rcs.constants.BaseConstants;
//import com.panda.sport.rcs.mapper.RcsOddsConvertMappingMapper;
//import com.panda.sport.rcs.mapper.RcsOddsConvertMappingMyMapper;
//import com.panda.sport.rcs.pojo.RcsOddsConvertMapping;
//import com.panda.sport.rcs.pojo.RcsOddsConvertMappingMy;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.math.NumberUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
///**
// * @author :  Sean
// * @Project Name :  panda-rcs-trade
// * @Package Name :  com.panda.sport.rcs.trade.service
// * @Description :  TODO
// * @Date: 2020-10-03 16:20
// * @ModificationHistory Who    When    What
// * --------  ---------  --------------------------
// */
//@Service
//@Slf4j
//public class RcsOddsConvertMappingService {
//    @Autowired
//    private RcsOddsConvertMappingMapper rcsOddsConvertMappingMapper;
//    @Autowired
//    private RcsOddsConvertMappingMyMapper rcsOddsConvertMappingMyMapper;
//
//    /**
//     * @Description   //根据马来赔获取最大的欧赔
//     * @Param [myOdds]
//     * @Author  Sean
//     * @Date  16:38 2020/10/3
//     * @return java.lang.String
//     **/
//    public String maxEUOddsByMYodds(String myOdds){
//        String maxOdds = NumberUtils.INTEGER_ZERO.toString();
//        QueryWrapper<RcsOddsConvertMapping> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(RcsOddsConvertMapping ::getMalaysia,myOdds);
//        List<RcsOddsConvertMapping> rcsOddsConvertMappings = rcsOddsConvertMappingMapper.selectList(queryWrapper);
//        if (CollectionUtils.isNotEmpty(rcsOddsConvertMappings)){
//            Collections.sort(rcsOddsConvertMappings,new Comparator<RcsOddsConvertMapping>(){
//                @Override
//                public int compare(RcsOddsConvertMapping arg0, RcsOddsConvertMapping arg1) {
//                    return new BigDecimal(arg1.getEurope()).compareTo(new BigDecimal(arg0.getEurope()));
//                }
//            });
//            maxOdds = rcsOddsConvertMappings.get(NumberUtils.INTEGER_ZERO).getEurope();
//        }
//        return maxOdds;
//    }
//    /**
//     * @Description   //根据10w倍欧赔获取马来赔
//     * @Param [euOdds]
//     * @Author  Sean
//     * @Date  16:49 2020/10/3
//     * @return java.lang.String
//     **/
//    public String getMyOdds(Integer euOdds){
//        BigDecimal originalOddsValue = new BigDecimal(euOdds).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),2,BigDecimal.ROUND_DOWN);
//        String odds = getMyOdds(originalOddsValue.toString());
//        return odds;
//    }
//    /**
//     * @Description   //根据两位小数的欧赔获取马来盘
//     * @Param [euOdds]
//     * @Author  Sean
//     * @Date  16:50 2020/10/3
//     * @return java.lang.String
//     **/
//    public String getMyOdds(String euOdds){
//        String odds = NumberUtils.INTEGER_ZERO.toString();
//        QueryWrapper<RcsOddsConvertMapping> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(RcsOddsConvertMapping ::getEurope,euOdds);
//        RcsOddsConvertMapping rcsOddsConvertMapping = rcsOddsConvertMappingMapper.selectOne(queryWrapper);
//        if (ObjectUtils.isNotEmpty(rcsOddsConvertMapping) && StringUtils.isNotBlank(rcsOddsConvertMapping.getMalaysia())){
//            odds = rcsOddsConvertMapping.getMalaysia();
//        }
//        return odds;
//    }
//    /**
//     * @Description   //根据马来赔获取欧赔
//     * @Param [myOdds]
//     * @Author  Sean
//     * @Date  16:50 2020/10/3
//     * @return java.lang.String
//     **/
//    public String getEUOdds(String myOdds){
//        String odds = NumberUtils.INTEGER_ZERO.toString();
//        QueryWrapper<RcsOddsConvertMappingMy> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(RcsOddsConvertMappingMy :: getMalaysia,myOdds);
//        RcsOddsConvertMappingMy rcsOddsConvertMapping = rcsOddsConvertMappingMyMapper.selectOne(queryWrapper);
//        if (ObjectUtils.isNotEmpty(rcsOddsConvertMapping) && StringUtils.isNotBlank(rcsOddsConvertMapping.getEurope())){
//            odds = rcsOddsConvertMapping.getEurope();
//        }
//        return odds;
//    }
//
//
//    public static void main(String[] args) {
//        RcsOddsConvertMapping user1 = new RcsOddsConvertMapping();
//        user1.setEurope("10.1");
//        RcsOddsConvertMapping user2 = new RcsOddsConvertMapping();
//        user2.setEurope("6.2");
//        RcsOddsConvertMapping user3 = new RcsOddsConvertMapping();
//        user3.setEurope("1.2");
//        RcsOddsConvertMapping user4 = new RcsOddsConvertMapping();
//        user4.setEurope("3.2");
//
//        List<RcsOddsConvertMapping> list = new ArrayList<RcsOddsConvertMapping>();
//        list.add(user1);
//        list.add(user2);
//        list.add(user3);
//        list.add(user4);
//
//
//        Collections.sort(list, new Comparator<RcsOddsConvertMapping>() {
//            @Override
//            public int compare(RcsOddsConvertMapping arg0, RcsOddsConvertMapping arg1) {
//                return new BigDecimal(arg1.getEurope()).compareTo(new BigDecimal(arg0.getEurope()));
//            }
//        });
//        for (RcsOddsConvertMapping u : list) {
//            System.out.println(u.getEurope());
//        }
//    }
//    /**
//     * @Description   //根据马来*10w得到欧赔
//     * @Param [intValue]
//     * @Author  Sean
//     * @Date  11:01 2020/10/9
//     * @return java.lang.String
//     **/
//    public String getEUOdds(Integer intValue) {
//        intValue = ObjectUtils.isEmpty(intValue) ? NumberUtils.INTEGER_ZERO : intValue;
//        String odds = new BigDecimal(intValue).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN).toString();
//        return getEUOdds(odds);
//    }
//}
