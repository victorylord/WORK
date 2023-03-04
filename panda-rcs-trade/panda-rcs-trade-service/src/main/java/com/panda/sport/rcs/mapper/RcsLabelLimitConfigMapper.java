package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.pojo.RcsLabelLimitConfig;
import com.panda.sport.rcs.vo.RcsLabelLimitConfigVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-04 12:20
 **/
@Component
public interface  RcsLabelLimitConfigMapper  extends BaseMapper<RcsLabelLimitConfig> {
    /**
     *
     * @return
     */
    IPage<RcsLabelLimitConfigVo> selectRcsLabelLimitConfig( IPage<RcsLabelLimitConfigVo> rcsLabelLimitConfigVoIPage);

    /**
     *
     * @param tagIdList
     * @return
     */
    void removeRcsLabelLimitConfigs(@Param("tagIdList") List<Integer> tagIdList);
    /**
     * @param tagIdList
     * 获取标签配置
     * */
    List<RcsLabelLimitConfig> getRcsLabelLimitConfigs();
}
