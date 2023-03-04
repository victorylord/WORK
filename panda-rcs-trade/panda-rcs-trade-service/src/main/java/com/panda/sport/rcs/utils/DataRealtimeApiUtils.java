package com.panda.sport.rcs.utils;

import com.alibaba.fastjson.JSONObject;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.ResultCode;
import com.panda.merge.dto.StandardMatchMarketDTO;
import com.panda.merge.dto.TradeMarketAutoDiffConfigDTO;
import com.panda.merge.dto.TradeMarketConfigDTO;
import com.panda.merge.dto.TradeMarketMarginConfigDTO;
import com.panda.merge.dto.TradeMarketPlaceConfigDTO;
import com.panda.merge.dto.TradeMarketStatusConfigDTO;
import com.panda.merge.dto.TradeMarketUiConfigDTO;
import com.panda.sport.manager.api.dto.ChangeLiveWeightDTO;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.Trace;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class DataRealtimeApiUtils {

    private static Map<String, String> requestMap = new HashMap<String, String>() {{
        put(ChangeLiveWeightDTO.class.getName(), "weight");
        // 盘口位置状态
        put(TradeMarketPlaceConfigDTO.class.getName(), "place");
        // 赔率
        put(StandardMatchMarketDTO.class.getName(), "odds");
        // 赛事配置，赛事状态、操盘方式等
        put(TradeMarketConfigDTO.class.getName(), "config");
        //margain
        put(TradeMarketMarginConfigDTO.class.getName(), "margin");
        //margain
        put(TradeMarketAutoDiffConfigDTO.class.getName(), "autodiff");
        //合并接口
        put(TradeMarketUiConfigDTO.class.getName(), "merge");
        // 盘口弃用
        put(TradeMarketStatusConfigDTO.class.getName(), "disable");
    }};

    public static <T, R> Response<R> handleApi(T params, ApiCall call) {
        Request<T> request = new Request<T>();
        request.setData(params);
        request.setDataSourceTime(System.currentTimeMillis());
        if (requestMap.containsKey(params.getClass().getName())) {
            request.setLinkId(UUID.randomUUID().toString().replace("-", "") + "_" + requestMap.get(params.getClass().getName()) + "_trade");
        } else {
            request.setLinkId(UUID.randomUUID().toString().replace("-", "") + "_trade");
        }
        request.setOperaterId(TradeUserUtils.getUserIdNoException().longValue());
        log.info("::{}::操作开始，参数类：{}，request：{}", request.getLinkId(),params.getClass(), JsonFormatUtils.toJson(request));
        Response<R> response = call.callApi(request);
        log.info("::{}::操作结果，参数类：{}，response：{}", request.getLinkId(), params.getClass(),JSONObject.toJSONString(response));
        if (response.isSuccess() || response.getCode() == ResultCode.CHANGETRADEFAILED.getCode().longValue()) {
            return response;
        }
        throw new RcsServiceException("融合异常：" + response.getMsg() + ", linkId:" + request.getLinkId());
    }

    public static <T, R> Response<R> handleApi(String linkId, T params, ApiCall call) {
        Request<T> request = new Request<>();
        request.setData(params);
        request.setDataSourceTime(System.currentTimeMillis());
        request.setLinkId(linkId);
        request.setOperaterId(TradeUserUtils.getUserIdNoException().longValue());
        log.info("::{}::操作开始，参数类：{}，request：{}", request.getLinkId(),params.getClass(), JsonFormatUtils.toJson(request));
        Response<R> response = call.callApi(request);
        log.info("::{}::操作结果，参数类：{}，response：{}", request.getLinkId(),params.getClass(), JSONObject.toJSONString(response));
        if (response.isSuccess() || response.getCode() == ResultCode.CHANGETRADEFAILED.getCode().longValue()) {
            return response;
        }
        throw new RcsServiceException("融合异常：" + response.getMsg() + ", linkId:" + request.getLinkId());
    }

    public static interface ApiCall {

        public <R> Response<R> callApi(Request request);

    }

    public static void main(String[] args) {
        System.out.println(ChangeLiveWeightDTO.class.getSimpleName());
        ChangeLiveWeightDTO weightBean = new ChangeLiveWeightDTO();
        Response<String> response = DataRealtimeApiUtils.handleApi(weightBean, new ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                System.out.println(JSONObject.toJSONString(request));
                return null;
            }
        });
    }

}
