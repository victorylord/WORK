package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.pojo.RcsOddsConvertMapping;
import com.panda.sport.rcs.vo.SportMarketCategoryVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 赔率转换映射表 服务类
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Service
public interface RcsOddsConvertMappingService extends IService<RcsOddsConvertMapping> {

    /**
     * 获取所有的赔率转换数据
     *  主键欧赔
     * @return
     */
    Map<String, Map<String, String>> listRcsOddsConvertMapping();


    /**
     * 查询玩法下面配置的盘口类型
     * @param ids  多个玩法id，例如：1,2,3
     * @return
     */
    List<SportMarketCategoryVo> listStandardSportMarketCategory(String ids);


    /**
     * 获取降级后的赔率
     * @param displayOddsVal
     * @return
     */
	String getNextLevelOdds(String displayOddsVal);

    /**
     * @return java.lang.String
     * @Description //赔率转换
     * @Param [odds, marketKindEnum]
     * @Author kimi
     * @Date 2019/12/27
     **/
    String getOddsValue(String odds, MarketKindEnum marketKindEnum);
    /**
     * @Description   //根据马来赔获取最大的欧赔
     * @Param [myOdds]
     * @Author  Sean
     * @Date  11:17 2020/10/17
     * @return java.lang.String
     **/
    String maxEUOddsByMYOdds(String myOdds);
    /**
     * @Description   //根据马来赔获取最小的欧赔
     * @Param [myOdds]
     * @Author  Sean
     * @Date  11:17 2020/10/17
     * @return java.lang.String
     **/
    String minEUOddsByMYOdds(String myOdds);
    /**
     * @Description   //根据10w倍欧赔获取马来赔
     * @Param [euOdds]
     * @Author  Sean
     * @Date  11:11 2020/10/17
     * @return java.lang.String
     **/
    String getMyOdds(Integer euOdds);
    /**
     * @Description   //根据两位小数的欧赔获取马来盘
     * @Param [euOdds]
     * @Author  Sean
     * @Date  11:10 2020/10/17
     * @return java.lang.String
     **/
    String getMyOdds(String euOdds);
    /**
     * @Description   //根据马来赔获取欧赔
     * @Param [myOdds]
     * @Author  Sean
     * @Date  11:10 2020/10/17
     * @return java.lang.String
     **/
    String getEUOdds(String myOdds);
    /**
     * @Description   //根据马来*10w得到欧赔
     * @Param [intValue]
     * @Author  Sean
     * @Date  11:10 2020/10/17
     * @return java.lang.String
     **/
    String getEUOdds(Integer intValue);
    /**
     * @Description   //获取10w倍欧赔
     * @Param [myOdds]
     * @Author  sean
     * @Date   2020/12/3
     * @return java.lang.Integer
     **/
    Integer getEUOddsInteger(String myOdds);

    /**
     * 马赔转100000倍欧赔
     *
     * @param myOdds
     * @return
     */
    int myOddsToOddsValue(BigDecimal myOdds);

}
