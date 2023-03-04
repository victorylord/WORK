package com.panda.sport.rcs.trade.vo;

import com.panda.sports.api.vo.ShortSysUserVO;
import com.panda.sport.rcs.pojo.TUserBetRate;
import lombok.Data;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-05 16:19
 **/
@Data
public class RcsUserSpecialBetLimitConfigsVo {
    private RcsUserConfigVo rcsUserConfigVo;
    private List<RcsUserSpecialBetLimitConfigDataVo> rcsUserSpecialBetLimitConfigDataVoList;

    private ShortSysUserVO traderData;

    private List<TUserBetRate> userBetRateList;

    /**
     * 提交类型  1 提交商户决策 2 强制执行  3.商户掉我们的接口
     */
    private Integer submitType;

    /**
     * 风控补充说明（前端传过来的备注，可能为null）
     */
    private String supplementExplain;

    /**
     * 操盘手ID
     */
    private Integer traderId;
}
