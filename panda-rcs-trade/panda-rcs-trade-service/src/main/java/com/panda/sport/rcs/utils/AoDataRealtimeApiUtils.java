package com.panda.sport.rcs.utils;

import com.alibaba.fastjson.JSONObject;
import com.panda.aoodds.sports.api.entity.MatchTemplateConfigEntity;
import com.panda.aoodds.sports.api.entity.Request;
import com.panda.aoodds.sports.api.entity.Response;
import com.panda.aoodds.sports.api.entity.ResultCode;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class AoDataRealtimeApiUtils {
    public static <T, R> Response<R> handleApi(T params, ApiCall call) {
        Request<T> request = new Request<T>();
        request.setData(params);
        request.setDataSourceTime(System.currentTimeMillis());
        request.setLinkId(UUID.randomUUID().toString().replace("-", "") + "_ao");

        request.setOperaterId(TradeUserUtils.getUserIdNoException().longValue());
        log.info("AO操作开始，参数类：{}，request：{}", params.getClass(), JsonFormatUtils.toJson(request));
        Response<R> response = call.callApi(request);
        log.info("AO操作结果，参数类：{}，response：{}", params.getClass(), JSONObject.toJSONString(response));
        if (response.isSuccess() || response.getCode() == ResultCode.CHANGETRADEFAILED.getCode().longValue()) {
            return response;
        }
        throw new RcsServiceException("AO融合异常：" + response.getMsg() + ", linkId:" + request.getLinkId());
    }

    public static <T, R> Response<R> handleApi(String linkId, T params, ApiCall call) {
        Request<T> request = new Request<>();
        request.setData(params);
        request.setDataSourceTime(System.currentTimeMillis());
        request.setLinkId(linkId);
        request.setOperaterId(TradeUserUtils.getUserIdNoException().longValue());
        log.info("AO操作开始，参数类：{}，request：{}", params.getClass(), JsonFormatUtils.toJson(request));
        Response<R> response = call.callApi(request);
        log.info("AO操作结果，参数类：{}，response：{}", params.getClass(), JSONObject.toJSONString(response));
        if (response.isSuccess() || response.getCode() == ResultCode.CHANGETRADEFAILED.getCode().longValue()) {
            return response;
        }
        throw new RcsServiceException("AO融合异常：" + response.getMsg() + ", linkId:" + request.getLinkId());
    }

    public static interface ApiCall {

        public <R> Response<R> callApi(Request request);

    }
}
