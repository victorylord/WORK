package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsUserConfigExt;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户特殊配置扩展
 *
 * @description:
 * @author: magic
 * @create: 2022-05-14 18:15
 **/
@Component
public interface RcsUserConfigExtMapper extends BaseMapper<RcsUserConfigExt> {

    long batchInsertOrUpdateTagMarketLevelStatus(@Param("list") List<RcsUserConfigExt> list);
}
