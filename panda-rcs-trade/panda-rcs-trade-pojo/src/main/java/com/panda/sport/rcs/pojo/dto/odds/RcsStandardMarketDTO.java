package com.panda.sport.rcs.pojo.dto.odds;

import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author :  sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.pojo.dto.odds
 * @Description :  TODO
 * @Date: 2020-12-02 14:44
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsStandardMarketDTO extends StandardMarketDTO {
    private static final long serialVersionUID = 1L;
    /**替换赔率顺序
        0-根据盘口id替换赔率；1-根据位置替换赔率; 2-根据盘口值替换赔率
     */
    private Integer oddsReplaceOrder;
    /*盘口id*/
    private String id;
    
    private String oldSpread;

    private Integer paStatus;

    public Double getNumberOfAddition1(){
        if (StringUtils.isBlank(this.getAddition1())){
            return NumberUtils.DOUBLE_ZERO;
        }else if (TradeConstant.MAIN_BASKETBALL_TOTAL.contains(this.getMarketCategoryId())){
            return new BigDecimal(this.getAddition1()).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).doubleValue();
        }
        try {
            return new BigDecimal(this.getAddition1()).doubleValue();
        }catch (NumberFormatException e){
            return NumberUtils.DOUBLE_ZERO;
        }
    }

    /**
     * 父ID
     */
    private String parentId;
    public String getParentId(){
        try {
            if(StringUtils.isNotEmpty(super.getId())){
                return super.getId();
            }else{
                return this.getId();
            }
        }catch (Exception e){

        }
        return "";
    }

}
