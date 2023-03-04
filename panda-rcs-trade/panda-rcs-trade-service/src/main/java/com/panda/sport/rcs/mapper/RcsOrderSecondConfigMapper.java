package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsOrderSecondConfig;
import com.panda.sport.rcs.vo.OrderSecondConfigVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author :  carver
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2020-11-17 20:27
 */
@Repository
public interface RcsOrderSecondConfigMapper extends BaseMapper<RcsOrderSecondConfig> {

    List<OrderSecondConfigVo> selectOrderSecondConfig(OrderSecondConfigVo param);

    void insertOrderSecondConfig(RcsOrderSecondConfig param);
    /**
     * @Description   //查询是否还有一键秒接配置
     * @Param [vo, time]
     * @Author  sean
     * @Date   2020/11/21
     * @return java.lang.Integer
     **/
    Integer selectOrderSecondConfigCount(@Param("vo") RcsOrderSecondConfig vo,@Param("time")Long time);

    List<String> selectOrderSecondTraders(@Param("vo") OrderSecondConfigVo vo,@Param("time")Long time);

    List<RcsOrderSecondConfig> selectOrderSecond(@Param("vo") RcsOrderSecondConfig vo,@Param("time")Long time);

    int updateOrderSecond(@Param("vo") RcsOrderSecondConfig vo);


}
