package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.pojo.RcsTraderMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  预警消息
 * @Date: 2020-09-16 16:38
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public interface RcsTraderMessageMapper extends BaseMapper<RcsTraderMessage> {
    void insertIgnoreInto(@Param("msgId") Long msgId,@Param("userId")String userId);
}
