package com.panda.sport.rcs.trade.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.pojo.tourTemplate.AoParameterTemplateReq;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.init.AoDataSourceInit;
import com.panda.sport.rcs.trade.service.AoParameterTemplateService;
import com.panda.sport.rcs.trade.vo.ao.AoParametersModifyVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.AoBasketBallTemplateConfigEntity;
import com.panda.sport.rcs.trade.vo.tourTemplate.AoFootBallTemplateConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;


/**
 * ao数据源业务类
 */
@Service
@Slf4j
public class AoParameterTemplateServiceImpl implements AoParameterTemplateService {
    private final RcsTournamentTemplateMapper templateMapper;
    private final AoDataSourceInit aoDataSourceInit;

    public AoParameterTemplateServiceImpl(RcsTournamentTemplateMapper templateMapper, AoDataSourceInit aoDataSourceInit) {
        this.templateMapper = templateMapper;
        this.aoDataSourceInit = aoDataSourceInit;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateAoParameterTemplate(AoParameterTemplateReq request) {
        if (!ObjectUtils.isEmpty(request.getTemplateId()) && !ObjectUtils.isEmpty(request)) {
            RcsTournamentTemplate template = new RcsTournamentTemplate();
            template.setId(request.getTemplateId());
            template.setAoConfigValue(request.getAoConfigValue());
            template.setSportId(request.getSportId());
            templateMapper.updateById(template);
            if (aoDataSourceInit.checkIfAoSport(request.getSportId())) {
                RcsTournamentTemplate tournamentTemplate = new RcsTournamentTemplate();
                BeanUtils.copyProperties(request, tournamentTemplate);
                aoDataSourceInit.sendAoDataSourceMessage(tournamentTemplate, request.getMatchId());
            }
        }

    }
}
