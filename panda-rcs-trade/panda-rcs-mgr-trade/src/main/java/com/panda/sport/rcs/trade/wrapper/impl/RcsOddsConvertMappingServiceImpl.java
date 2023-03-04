package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.mapper.RcsOddsConvertMappingMapper;
import com.panda.sport.rcs.mapper.RcsOddsConvertMappingMyMapper;
import com.panda.sport.rcs.pojo.RcsOddsConvertMapping;
import com.panda.sport.rcs.pojo.RcsOddsConvertMappingMy;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketCategoryService;
import com.panda.sport.rcs.utils.BigDecimalUtils;
import com.panda.sport.rcs.vo.SportMarketCategoryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 赔率转换映射表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
@Slf4j
public class RcsOddsConvertMappingServiceImpl extends ServiceImpl<RcsOddsConvertMappingMapper, RcsOddsConvertMapping> implements RcsOddsConvertMappingService {
    //存放赔率枚举
    private static LinkedHashMap<String, String> map = new LinkedHashMap<>();

    private static  Map<String, Map<String, String>> rtnMap = new LinkedHashMap<>();

    public Map<String,String> myToEuOddsMap = Maps.newHashMap();
    private Map<String,String> euToMyOddsMyMap = Maps.newHashMap();

    private Map<String,String> euToMyOddsMap = Maps.newHashMap();

    private static String  MIN_ODDS_VALUE;
    private static String  MAX_ODDS_VALUE;
    @Autowired
    private StandardSportMarketCategoryService standardSportMarketCategoryService;
    @Autowired
    private RcsOddsConvertMappingMapper rcsOddsConvertMappingMapper;
    @Autowired
    private RcsOddsConvertMappingMyMapper rcsOddsConvertMappingMyMapper;
    @Autowired
    private RcsOddsConvertMappingMapper mapper;
	BigDecimal bigDecimal = new BigDecimal(BaseConstants.MULTIPLE_VALUE);
    public void init(){
    	List<RcsOddsConvertMapping> listRcsOddsConvertMapping = mapper.queryOddsMappingList();
    	if (!CollectionUtils.isEmpty(listRcsOddsConvertMapping)){
    	    MIN_ODDS_VALUE=new DecimalFormat("#.00").format(Double.parseDouble(listRcsOddsConvertMapping.get(0).getEurope()));
    	    MAX_ODDS_VALUE=new DecimalFormat("#.00").format(Double.parseDouble(listRcsOddsConvertMapping.get(listRcsOddsConvertMapping.size()-1).getEurope()));
    	    for (int x=0;x<listRcsOddsConvertMapping.size();x++) {
                String format = new DecimalFormat("#.00").format(Double.parseDouble(listRcsOddsConvertMapping.get(x).getEurope()));
                map.put(format,format );
    	    }
    	}
        List<RcsOddsConvertMapping> list = rcsOddsConvertMappingMapper.selectList(null);
        euToMyOddsMap = list.stream().collect(Collectors.toMap(e -> e.getEurope(), e ->e.getMalaysia()));

        List<RcsOddsConvertMappingMy> myList = rcsOddsConvertMappingMyMapper.selectList(null);
        myToEuOddsMap = myList.stream().collect(Collectors.toMap(e -> e.getMalaysia(), e ->e.getEurope()));
        euToMyOddsMyMap = myList.stream().collect(Collectors.toMap(e -> e.getEurope(), e ->e.getMalaysia()));

        rtnMap = listRcsOddsConvertMapping();
    }

    @Override
    public Map<String, Map<String, String>> listRcsOddsConvertMapping() {
        List<RcsOddsConvertMapping> listRcsOddsConvertMapping = this.list();
        if (CollectionUtils.isEmpty(listRcsOddsConvertMapping)) {
            log.warn("::{}::execute rcsOddsConvertMapping list values isEmpty.",CommonUtil.getRequestId());
            return Collections.emptyMap();
        }
        log.info("::{}::execute rcsOddsConvertMapping list values size:{}",CommonUtil.getRequestId(), listRcsOddsConvertMapping.size());

        Map<String, Map<String, String>> rtnMap = new LinkedHashMap<>(listRcsOddsConvertMapping.size());
        listRcsOddsConvertMapping.forEach(model -> {
            //根据前端要求，拼接Key值
			String eu = MarketKindEnum.Europe.getValue();
            eu = eu + "_" + model.getEurope();
            //组装所有赔率转换数据
            Map<String, String> map = Maps.newHashMap();
            if (model.getEurope()!=null){
                map.put(MarketKindEnum.Europe.getValue(), model.getEurope().trim());
            }
            if (model.getMalaysia()!=null){
                map.put(MarketKindEnum.Malaysia.getValue(),model.getMalaysia().trim() );
            }
            rtnMap.put(eu, map);
        });
        return rtnMap;
    }


	@Override
    public List<SportMarketCategoryVo> listStandardSportMarketCategory(String ids) {
        log.info("::{}::listStandardSportMarketCategory method params>>>ids:{}",CommonUtil.getRequestId(), ids);
        Object[] idLists = ids.split(",");
        //根据玩法id，获取标准玩法数据
        QueryWrapper<StandardSportMarketCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(StandardSportMarketCategory::getId, idLists);
        List<StandardSportMarketCategory> list = standardSportMarketCategoryService.list(queryWrapper);
        log.info("::{}::standardSportMarketCategoryService.list size:{}",CommonUtil.getRequestId(), list.size());
        //定义返回结果集变量
        List<SportMarketCategoryVo> sportMarketCategoryVoList = Lists.newArrayListWithCapacity(list.size());
        list.forEach(standardSportMarketCategory -> {
            SportMarketCategoryVo sportMarketCategoryVo = new SportMarketCategoryVo();
            sportMarketCategoryVo.setId(standardSportMarketCategory.getId());
            //获取盘口类型id
            String oddsSwitch = standardSportMarketCategory.getOddsSwitch();
            if (!StringUtils.isEmpty(oddsSwitch)) {
                String[] oddsSwitchIds = oddsSwitch.split(",");
                List oddsSwitchList = Arrays.asList(oddsSwitchIds);
                sportMarketCategoryVo.setOddsSwitch(oddsSwitchList);
            }
            sportMarketCategoryVoList.add(sportMarketCategoryVo);
        });
        return sportMarketCategoryVoList;
    }
	
	@Override
	public String getNextLevelOdds(String displayOddsVal) {
        if(displayOddsVal == null) {
		    return null;
		}
        double oddValue = Double.parseDouble(displayOddsVal);
        if (oddValue<Double.parseDouble(MIN_ODDS_VALUE)){
            return map.get(MIN_ODDS_VALUE);
        }
        if (oddValue>Double.parseDouble(MAX_ODDS_VALUE)){
            return map.get(MAX_ODDS_VALUE);
        }
        displayOddsVal = new DecimalFormat("#.00").format(oddValue);
        String s=null;
        if (!CollectionUtils.isEmpty(map)){
             s= map.get(displayOddsVal);
            if (s==null){
                BigDecimal multiply=new BigDecimal(displayOddsVal);
                while (true){
                    if (map.containsKey(multiply.toPlainString())){
                        return map.get(multiply);
                    }
                    multiply=multiply.subtract(new BigDecimal("0.01"));
                    //避免无效循环
                    if (multiply.doubleValue()<Double.parseDouble(MIN_ODDS_VALUE)  ){
                        return map.get(MIN_ODDS_VALUE);
                    }
                }
            }else {
                return s;
            }

        }
        return s;
	}

	@Override
	public String getOddsValue(String odds, MarketKindEnum marketKindEnum) {
		BigDecimal divide = new BigDecimal(odds).setScale(2);
		double v = Double.parseDouble(odds);
		if (v > 1000 || v < -1000) {
			divide = new BigDecimal(odds).divide(bigDecimal, 2, RoundingMode.DOWN);
		}
		String fieldOddsValue = getNextLevelOdds(divide.toPlainString());
		//Map<String, Map<String, String>> stringMapMap = listRcsOddsConvertMapping();
		String s1 = MarketKindEnum.Europe.getValue() + "_" + fieldOddsValue;
		//如果赔率不存在  则直接返回0的
		if (!rtnMap.containsKey(s1)) {
			log.error("欧赔出现了赔率为0的数值");
			return "0";
		} else {
			String s = rtnMap.get(s1).get(marketKindEnum.getValue());
			return s;
		}
	}
    /**
     * @Description   //根据马来赔获取最大的欧赔
     * @Param [myOdds]
     * @Author  Sean
     * @Date  16:38 2020/10/3
     * @return java.lang.String
     **/
    @Override
    public String maxEUOddsByMYOdds(String myOdds){
        String maxOdds = NumberUtils.INTEGER_ZERO.toString();
        myOdds = formatOdds(myOdds);
        String mapping = rcsOddsConvertMappingMapper.queryMaxOdds(myOdds);
        if (!StringUtils.isEmpty(mapping)){
            maxOdds = mapping;
        }
        return maxOdds;
    }
    /**
     * @Description   //根据马来赔获取最小的欧赔
     * @Param [myOdds]
     * @Author  Sean
     * @Date  16:38 2020/10/3
     * @return java.lang.String
     **/
    @Override
    public String minEUOddsByMYOdds(String myOdds){
        String maxOdds = NumberUtils.INTEGER_ZERO.toString();
        myOdds = formatOdds(myOdds);
        String mapping = rcsOddsConvertMappingMapper.queryMinOdds(myOdds);
        if (!StringUtils.isEmpty(mapping)){
            maxOdds = mapping;
        }
        return maxOdds;
    }
    /**
     * @Description   //赔率保存两位小数
     * @Param [myOdds]
     * @Author  Sean
     * @Date  14:58 2020/10/23
     * @return java.lang.String
     **/
    private String formatOdds(String myOdds) {
        return new BigDecimal(myOdds).divide(new BigDecimal(NumberUtils.DOUBLE_ONE),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * @Description   //根据10w倍欧赔获取马来赔
     * @Param [euOdds]
     * @Author  Sean
     * @Date  16:49 2020/10/3
     * @return java.lang.String
     **/
    @Override
    public String getMyOdds(Integer euOdds){
        BigDecimal originalOddsValue = new BigDecimal(euOdds).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),2,BigDecimal.ROUND_DOWN);
        String odds = getMyOdds(originalOddsValue.toString());
        return odds;
    }
    /**
     * @Description   //根据两位小数的欧赔获取马来盘
     * @Param [euOdds]
     * @Author  Sean
     * @Date  16:50 2020/10/3
     * @return java.lang.String
     **/
    @Override
    public String getMyOdds(String euOdds){
        euOdds = formatOdds(euOdds);
        String odds = euToMyOddsMyMap.get(euOdds);
        if (StringUtils.isEmpty(odds)){
            odds = euToMyOddsMap.get(euOdds);
            if (StringUtils.isEmpty(odds)){
                odds = NumberUtils.INTEGER_ZERO.toString();
            }
        }
//        QueryWrapper<RcsOddsConvertMapping> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(RcsOddsConvertMapping ::getEurope,euOdds);
//        RcsOddsConvertMapping rcsOddsConvertMapping = rcsOddsConvertMappingMapper.selectOne(queryWrapper);
//        if (ObjectUtils.isNotEmpty(rcsOddsConvertMapping) && org.apache.commons.lang3.StringUtils.isNotBlank(rcsOddsConvertMapping.getMalaysia())){
//            odds = rcsOddsConvertMapping.getMalaysia();
//        }
        return odds;
    }
    /**
     * @Description   //根据马来赔获取欧赔
     * @Param [myOdds]
     * @Author  Sean
     * @Date  16:50 2020/10/3
     * @return java.lang.String
     **/
    @Override
    public String getEUOdds(String myOdds){
        myOdds = formatOdds(myOdds);
        String odds = myToEuOddsMap.get(myOdds);
        if (StringUtils.isEmpty(odds)){
            odds = NumberUtils.INTEGER_ZERO.toString();
        }
        return odds;
    }
    /**
     * @Description   //根据马来赔获取10w倍欧赔
     * @Param [myOdds]
     * @Author  Sean
     * @Date  16:50 2020/10/3
     * @return java.lang.Integer
     **/
    @Override
    public Integer getEUOddsInteger(String myOdds){
        //测试打印
        log.info("specialSpreadCalculate3 myToEuOddsMap={}", JSONObject.toJSONString(myToEuOddsMap));
        myOdds = formatOdds(myOdds);
        String odds = myToEuOddsMap.get(myOdds);
        if (StringUtils.isEmpty(odds)){
            odds = NumberUtils.INTEGER_ZERO.toString();
        }
        Integer oddsValue = new BigDecimal(odds).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
        return oddsValue;
    }


    /**
     * @Description   //根据马来*10w得到欧赔
     * @Param [intValue]
     * @Author  Sean
     * @Date  11:01 2020/10/9
     * @return java.lang.String
     **/
    @Override
    public String getEUOdds(Integer intValue) {
        intValue = ObjectUtils.isEmpty(intValue) ? NumberUtils.INTEGER_ZERO : intValue;
        String odds = new BigDecimal(intValue).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN).toString();
        return getEUOdds(odds);
    }

    @Override
    public int myOddsToOddsValue(BigDecimal myOdds) {
        BigDecimal euOdds = BigDecimalUtils.toBigDecimal(getEUOdds(myOdds.toPlainString()), BigDecimal.ZERO);
        return BigDecimalUtils.ROUND_DOWN_2.multiply(euOdds, new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
    }

}
