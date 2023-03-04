package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsUserConfigNewMapper;
import com.panda.sport.rcs.pojo.RcsUserConfigNew;
import com.panda.sport.rcs.trade.vo.RcsUserConfigNewConfig;
import com.panda.sport.rcs.trade.vo.RcsUserConfigVo;
import com.panda.sport.rcs.trade.wrapper.IRcsUserConfigNewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RcsUserConfigNewServiceImpl extends ServiceImpl<RcsUserConfigNewMapper, RcsUserConfigNew> implements IRcsUserConfigNewService {

    @Override
    public Map<Long, RcsUserConfigVo> getByUserIds(List<Long> userIds) {
        List<RcsUserConfigNew> newList = baseMapper.selectList(new LambdaQueryWrapper<RcsUserConfigNew>().in(RcsUserConfigNew::getUserId, userIds));
        return newList.stream().collect(Collectors.toMap(RcsUserConfigNew::getUserId, e -> {
            RcsUserConfigVo rcsUserConfigVo = new RcsUserConfigVo();
            BeanUtils.copyProperties(e, rcsUserConfigVo);

            if (StringUtils.isNotBlank(e.getConfig())) {
                List<RcsUserConfigNewConfig> configList = JSONArray.parseArray(e.getConfig(), RcsUserConfigNewConfig.class);
                rcsUserConfigVo.setSportIdList(configList.stream().map(RcsUserConfigNewConfig::getSportId).distinct().collect(Collectors.toList()));
            }
            //类型兼容
            if (e.getSpecialBettingLimit() == null || e.getSpecialBettingLimit() == 0) {
                rcsUserConfigVo.setSpecialBettingLimit(1);
            }
            return rcsUserConfigVo;
        }));
    }

    @Override
    public Map<Long, RcsUserConfigVo> getByUserIds(Long... userIds) {
        return getByUserIds(Arrays.asList(userIds));
    }

    @Override
    public void save(RcsUserConfigVo rcsUserConfigVo, Integer traderId) {
        RcsUserConfigNew rcsUserConfigOld = baseMapper.selectOne(new LambdaQueryWrapper<RcsUserConfigNew>().eq(RcsUserConfigNew::getUserId, rcsUserConfigVo.getUserId()));

        RcsUserConfigNew rcsUserConfigNew = new RcsUserConfigNew();
        BeanUtils.copyProperties(rcsUserConfigVo, rcsUserConfigNew);
        if (rcsUserConfigOld != null) {
            rcsUserConfigNew.setId(rcsUserConfigOld.getId());
            rcsUserConfigNew.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        rcsUserConfigNew.setTradeId(traderId.longValue());
        if (rcsUserConfigVo.getSpecialBettingLimit() == null || rcsUserConfigVo.getSpecialBettingLimit() == 0) {
            rcsUserConfigNew.setSpecialBettingLimit(1);
        }
        if (CollectionUtils.isNotEmpty(rcsUserConfigVo.getSportIdList())) {
            rcsUserConfigNew.setConfig(JSONObject.toJSONString(rcsUserConfigVo.getSportIdList().stream().filter(Objects::nonNull).distinct().sorted().map(e -> {
                RcsUserConfigNewConfig rcsUserConfigNewConfig = new RcsUserConfigNewConfig();
                rcsUserConfigNewConfig.setSportId(e);
                return rcsUserConfigNewConfig;
            }).collect(Collectors.toList())));
        }
        if(StringUtils.isEmpty(rcsUserConfigNew.getTagMarketLevelId())){
            rcsUserConfigNew.setTagMarketLevelId(null);
        }
        saveOrUpdate(rcsUserConfigNew);
    }
}

