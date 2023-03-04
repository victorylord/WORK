package com.panda.sport.rcs.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-10-28 19:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MerchantMatchLimitReqVo {


    //百分比
    private Integer percentage = 70;

    //赛事id
    private List<String> matchIds;

    //商户
    private List<Long> merchant;

    //排序字段 0百分比 1商户  2赛事
    private Integer orderColumn = 0;

    //排序方式 0升序   1降序
    private Integer orderType = 1;

    Integer page = 1;

    Integer pageSize = 10;

    public static void main(String[] args) {
        MerchantMatchLimitReqVo vo = new MerchantMatchLimitReqVo();
        List<Long> matchIds = new ArrayList<>();
        matchIds.add(2L);
        matchIds.add(1L);
        List<Long> merchant= new ArrayList<>();
        merchant.add(1L);
        merchant.add(3L);
       // vo.setMatchIds(matchIds);
        vo.setMerchant(merchant);
        System.out.println(JSONObject.toJSONString(vo));
    }
}
