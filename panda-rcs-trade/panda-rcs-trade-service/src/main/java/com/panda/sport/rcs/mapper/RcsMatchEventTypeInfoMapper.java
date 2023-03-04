package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.pojo.RcsMatchEventTypeInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 多语言 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface RcsMatchEventTypeInfoMapper extends BaseMapper<RcsMatchEventTypeInfo> {
    /**
     * @Description   //根据数据库id更新事件说明
     * @Param [id]
     * @Author  Sean
     * @Date  9:50 2020/9/5
     **/
    void updateMatchEventById(@Param("info") RcsMatchEventTypeInfo info);
    /**
     * @Description   //分页查询事件信息
     * @Param [info]
     * @Author  sean
     * @Date   2020/11/10
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchEventTypeInfo>
     **/
    List<RcsMatchEventTypeInfo> selectEventPages(@Param("info") RcsMatchEventTypeInfo info);
    /**
     * @Description   //查询事件总数
     * @Param [info]
     * @Author  sean
     * @Date   2020/11/10
     * @return java.lang.Integer
     **/
    Integer selectEventPagesCount(@Param("info") RcsMatchEventTypeInfo info);
}
