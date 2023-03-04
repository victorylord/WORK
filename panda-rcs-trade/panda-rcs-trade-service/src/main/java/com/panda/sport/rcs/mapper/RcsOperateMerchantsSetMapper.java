package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsOperateMerchantsSet;
import com.panda.sport.rcs.pojo.vo.RcsOperateMerchantsSetVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-12-02 14:59
 **/
@Service
public interface RcsOperateMerchantsSetMapper extends BaseMapper<RcsOperateMerchantsSet> {
    /**
     * 获取商户数据
     * @return
     */
    List<RcsOperateMerchantsSetVo> selectRcsOperateMerchantsSet();

    List<RcsOperateMerchantsSet> selectAllMerchants();
}
