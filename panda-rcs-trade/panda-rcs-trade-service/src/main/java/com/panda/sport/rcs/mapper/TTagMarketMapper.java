package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.TTagMarket;
import com.panda.sport.rcs.pojo.TTagMarketReqVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 财务特征标签行情管控 Mapper
 * </p>
 *
 * @author CodeGenerator
 * @since 2021-04-08
 */
@Service
public interface TTagMarketMapper extends BaseMapper<TTagMarket> {

    List<TTagMarketReqVo> getTagMarketList();
}
