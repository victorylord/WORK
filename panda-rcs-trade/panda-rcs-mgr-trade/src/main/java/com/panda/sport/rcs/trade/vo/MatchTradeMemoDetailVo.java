package com.panda.sport.rcs.trade.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.pojo.RcsMatchTradeMemo;
import lombok.Data;

@Data
public class MatchTradeMemoDetailVo {

    private Long standardMatchId;

    private RcsMatchTradeMemo matchTradeMemo;

    private IPage<RcsMatchTradeMemo> memoPage;
}
