package com.panda.sport.rcs.trade.service;

import com.panda.sport.data.rcs.api.ExceptionData;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.api.StandardSportMarketCategoryApiService;
import com.panda.sport.rcs.constants.RcsErrorInfoConstants;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.DataSyncService;
import com.panda.sport.rcs.trade.base.AbstractApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@org.springframework.stereotype.Service
@Slf4j
public class StandardSportMarketCategoryApiImpl extends AbstractApiService<Long> implements StandardSportMarketCategoryApiService {

    @Resource(name = "marketCategorySyncService")
    DataSyncService dataSyncService;


    /**
     * 赛事投递接口-实时状况
     *
     * @param requestParam
     * @return
     */
    @Override
    public Response putSportMarketCategory(Request<Long> requestParam) {
        List<ExceptionData> resultList = null;
        try {
            resultList = this.dispatch(requestParam.getData());
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            return Response.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            return Response.error(RcsErrorInfoConstants.REQUEST_TO_RESPONSE_EXCEPTION, e.getMessage());
        }
        return Response.success(resultList);
    }

    /**
     * 接口入参校验
     *
     * @param requestData
     * @return
     */
    @Override
    protected List<ExceptionData> validation(Long requestData) {
        return null;
    }

    /**
     * 处理业务逻辑
     *
     * @param requestData
     * @return
     */
    @Override
    protected Map<String, String> doRequest(Long requestData) {
        return dataSyncService.receive(requestData);
    }

    /**
     * 通知下游系统：异步
     *
     * @param requestData
     */
    @Override
    protected void notifyDownstreamSystem(Long requestData) {

    }
}
