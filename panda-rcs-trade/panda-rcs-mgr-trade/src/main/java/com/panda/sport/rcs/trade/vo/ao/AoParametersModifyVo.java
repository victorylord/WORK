package com.panda.sport.rcs.trade.vo.ao;

import com.panda.sport.rcs.trade.vo.tourTemplate.AoBasketBallTemplateConfigEntity;
import com.panda.sport.rcs.trade.vo.tourTemplate.AoFootBallTemplateConfigEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: jstyChandler
 * @Package: com.panda.sport.rcs.pojo.vo
 * @ClassName: AoParametersModifyVo
 * @Description: TODO
 * @Date: 2023/2/10 17:09
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class AoParametersModifyVo {
    /**
     * 赛种ID
     */
    private Integer sportId;
    /**
     * 足球AO参数
     */
    private List<AoFootBallTemplateConfigEntity> footBallTemplateConfigList;
    /**
     * 篮球AO参数
     */
    private List<AoBasketBallTemplateConfigEntity> basketBallTemplateConfigList;

    public AoParametersModifyVo(){
    }

    public AoParametersModifyVo(Integer sportId){
        this.sportId = sportId;
    }

}
