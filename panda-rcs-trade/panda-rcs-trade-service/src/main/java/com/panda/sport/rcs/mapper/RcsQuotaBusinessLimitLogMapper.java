package com.panda.sport.rcs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.pojo.RcsQuotaBusinessLimitLog;
import com.panda.sport.rcs.pojo.vo.RcsQuotaBusinessLimitLogReqVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RcsQuotaBusinessLimitLogMapper extends BaseMapper<RcsQuotaBusinessLimitLog>  {

    RcsQuotaBusinessLimitLog selectByPrimaryKey(Long id);

    IPage<RcsQuotaBusinessLimitLog> queryByPage(IPage<RcsQuotaBusinessLimitLog> page, @Param("reqVo") RcsQuotaBusinessLimitLogReqVo reqVo);
    /**
     * 批量插入风控日志
     * */
    int bathInserts(List<RcsQuotaBusinessLimitLog> list);
}