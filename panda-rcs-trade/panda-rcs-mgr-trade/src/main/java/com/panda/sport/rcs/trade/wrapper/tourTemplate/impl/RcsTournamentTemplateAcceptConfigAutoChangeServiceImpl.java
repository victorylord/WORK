package com.panda.sport.rcs.trade.wrapper.tourTemplate.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigAutoChangeMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfigAutoChange;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.RcsTournamentTemplateAcceptConfigAutoChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class RcsTournamentTemplateAcceptConfigAutoChangeServiceImpl extends ServiceImpl<RcsTournamentTemplateAcceptConfigAutoChangeMapper, RcsTournamentTemplateAcceptConfigAutoChange> implements RcsTournamentTemplateAcceptConfigAutoChangeService {

    @Autowired
    private RcsTournamentTemplateAcceptConfigAutoChangeMapper mapper;

    @Autowired
    private RcsTournamentTemplateMapper templateMapper;

    @Autowired
    private RedisClient redisClient;

    @Override
    public void updateTemplateAcceptConfigAutoChange(RcsTournamentTemplateAcceptConfigAutoChange config) {
//        //kir-1788-修改开关
        UpdateWrapper<RcsTournamentTemplateAcceptConfigAutoChange> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("template_id", config.getTemplateId());
        updateWrapper.eq("category_set_id", config.getCategorySetId());
        mapper.update(config, updateWrapper);
        QueryWrapper<RcsTournamentTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", config.getTemplateId());
        RcsTournamentTemplate template = templateMapper.selectOne(queryWrapper);
        if(!ObjectUtils.isEmpty(template)){
            //赛事模板自动接拒开关（0.关 1.开）
            String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getRcsTournamentTemplateAcceptAutoChangeKey(template.getTypeVal(), config.getCategorySetId());
            redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, String.valueOf(config.getIsOpen()), 3600 * 24L);
        }
    }

    @Override
    public RcsTournamentTemplateAcceptConfigAutoChange queryTemplateAcceptConfigAutoChange(RcsTournamentTemplateAcceptConfigAutoChange configAutoChange) {
        QueryWrapper<RcsTournamentTemplateAcceptConfigAutoChange> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_id", configAutoChange.getTemplateId());
        queryWrapper.eq("category_set_id", configAutoChange.getCategorySetId());
        return mapper.selectOne(queryWrapper);
    }
}
