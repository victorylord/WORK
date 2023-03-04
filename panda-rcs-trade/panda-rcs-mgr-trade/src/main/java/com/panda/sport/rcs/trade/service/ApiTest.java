package com.panda.sport.rcs.trade.service;

import com.google.common.collect.Lists;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.dto.tournament.TournamentTemplateDTO;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.dto.UserExceptionDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping(value = "/api")
@Slf4j
public class ApiTest {
    @Autowired
    private RcsTournamentTemplateApiImpl rcsTournamentTemplateApi;
    @PostMapping("/queryTournamentLevelTemplate")
    public Response queryTournamentLevelTemplate() {
        Request<TournamentTemplateDTO> request = new Request();
        TournamentTemplateDTO dto = new TournamentTemplateDTO();
        dto.setSportId(1);
        dto.setTournamentId(1511L);
        dto.setMatchType(1);
        request.setData(dto);
        return rcsTournamentTemplateApi.queryTournamentLevelTemplate(request);
    }

    @PostMapping("/putTemplateToMatchTemplate")
    public Response putTemplateToMatchTemplate() {
        Request<TournamentTemplateDTO> request = new Request();
        TournamentTemplateDTO dto = new TournamentTemplateDTO();
        dto.setRiskManagerCode("PA");
        dto.setIsCurrentTemp(1);
        dto.setStandardMatchId(78789L);
        dto.setSportId(1);
        dto.setTournamentLevel(1);
        dto.setTournamentId(1109L);
        dto.setMatchType(0);
        dto.setTemplateId(407L);
        request.setData(dto);
        return rcsTournamentTemplateApi.putTemplateToMatchTemplate(request);
    }

    @PostMapping("/putMatchTemplateCancel")
    public Response putMatchTemplateCancel() {
        Request<TournamentTemplateDTO> request = new Request();
        TournamentTemplateDTO dto = new TournamentTemplateDTO();
        dto.setStandardMatchId(50099L);
        dto.setSportId(1);
        dto.setMatchType(1);
        request.setData(dto);
        try {
            Response s = rcsTournamentTemplateApi.putMatchTemplateCancel(request);
            return s;
        } catch (RcsServiceException ex) {
            return Response.error(500, ex.getErrorMassage());
        }
    }

    @PostMapping("/putMatchReplaceRiskManagerCode")
    public Response putMatchReplaceRiskManagerCode() {
        Request<TournamentTemplateDTO> request = new Request();
        TournamentTemplateDTO dto = new TournamentTemplateDTO();
        dto.setStandardMatchId(7856L);
        dto.setSportId(1);
        dto.setMatchType(0);
        request.setData(dto);
        request.setGlobalId("1234567");
        try {
            Response s = rcsTournamentTemplateApi.putMatchReplaceRiskManagerCode(request);
            return s;
        } catch (RcsServiceException ex) {
            return Response.error(500, ex.getErrorMassage());
        }
    }

    @PostMapping("/queryAllTournamentTemplateById")
    public Response queryAllTournamentTemplateById() {
        Request<TournamentTemplateDTO> request = new Request();
        TournamentTemplateDTO dto = new TournamentTemplateDTO();
        List<Long> a = Lists.newArrayList();
        a.add(14L);
        a.add(27L);
        dto.setTournamentIds(a);
        dto.setSportId(1);
        request.setData(dto);
        request.setGlobalId("1234567");
        try {
            Response s = rcsTournamentTemplateApi.queryAllTournamentTemplateById(request);
            return s;
        } catch (RcsServiceException ex) {
            return Response.error(500, ex.getErrorMassage());
        }
    }

    @PostMapping("/queryMatchTemplateByMatchId")
    public Response queryMatchTemplateByMatchId() {
        Request<List<Long>> request = new Request();
        List<Long> a = Lists.newArrayList();
        a.add(95942L);
        a.add(70735L);
        request.setData(a);
        request.setGlobalId("1234567");
        try {
            Response s = rcsTournamentTemplateApi.queryMatchTemplateByMatchId(request);
            return s;
        } catch (RcsServiceException ex) {
            return Response.error(500, ex.getErrorMassage());
        }
    }
}
