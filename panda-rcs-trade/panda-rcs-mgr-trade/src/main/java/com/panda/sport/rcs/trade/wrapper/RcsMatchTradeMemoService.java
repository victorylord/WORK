package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchTradeMemo;
import com.panda.sport.rcs.trade.vo.MatchTradeMemoDetailVo;

/**
 * @ClassName RcsMatchTradeMemoService
 * @Description: TODO
 * @Author riben
 * @Date 2021/2/3
 **/
public interface RcsMatchTradeMemoService extends IService<RcsMatchTradeMemo> {

    IPage<RcsMatchTradeMemo> getMemoPage(RcsMatchTradeMemo matchTradeMemo, Boolean byCondition);

    Boolean saveMemo(RcsMatchTradeMemo matchTradeMemo);

    MatchTradeMemoDetailVo getMemoDetail(RcsMatchTradeMemo matchTradeMemo);
}
