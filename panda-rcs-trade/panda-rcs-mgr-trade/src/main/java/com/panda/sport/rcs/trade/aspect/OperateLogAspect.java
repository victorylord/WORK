package com.panda.sport.rcs.trade.aspect;

import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.trade.strategy.logFormat.*;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.utils.SpringContextUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @OperateLog 操盤日誌AOP
 */
@Slf4j
@Aspect
@Component
public class OperateLogAspect {

    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(20, 40, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000), new ThreadPoolExecutor.CallerRunsPolicy());

    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Around("@annotation(com.panda.sport.rcs.log.annotion.OperateLog)")
    public Object operLog(ProceedingJoinPoint joinPoint) throws Throwable {

        beforeProcess(joinPoint);
        Object resultObj = joinPoint.proceed();
        afterProcess(joinPoint, resultObj);
        return resultObj;
    }

    private void beforeProcess(ProceedingJoinPoint joinPoint) {
        try {
            log.info("操盤日誌-執行前準備流程開始");
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogFormatStrategy logFormatStrategy = filterMethod(method.getName());
            logFormatStrategy.beforeProcess(joinPoint.getArgs());
            log.info("操盤日誌-執行前傳入參數={}", JsonFormatUtils.toJson(joinPoint.getArgs()));
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        } finally {
            log.info("操盤日誌-執行前準備流程結束");
        }

    }

    private void afterProcess(ProceedingJoinPoint joinPoint, Object resultObj) {
        try {
            HttpResponse result = (HttpResponse) resultObj;
            if (200 == result.getCode()) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Method method = signature.getMethod();
                OperateLog annotation = method.getAnnotation(OperateLog.class);
                LogFormatStrategy logFormatStrategy = filterMethod(method.getName());
                RcsOperateLog preLogBean = new RcsOperateLog(annotation);
                pool.execute(() -> {
                    try {
                        log.info("操盤日誌-流程開始");
                        RcsOperateLog rcsOperateLog = logFormatStrategy.formatLogBean(preLogBean, joinPoint.getArgs());
                        log.info("操盤日誌-流程傳入參數={}", JsonFormatUtils.toJson(joinPoint.getArgs()));
                        if (rcsOperateLog != null) {
                            sendMessage.sendMessage("rcs_log_operate", "", rcsOperateLog.getMatchId() + "", rcsOperateLog);
                        }
                    } catch (Exception e) {
                        log.error("::{}::操盤日誌-策略流程异常{}:", CommonUtil.getRequestId(), logFormatStrategy.getClass().getName(), e);
                    } finally {
                        log.info("操盤日誌-流程結束");
                    }
                });
            }
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
    }

    /**
     * 根據方法名稱判斷要導向的Format
     *
     * @param name
     * @return
     * @throws Exception
     */
    private LogFormatStrategy filterMethod(String name) throws Exception {

        switch (name) {
            case "updateAoParameterTemplate":
                //AO参数调整赛制
                return SpringContextUtils.getBeanByClass(LogAoCsChangeFormat.class);
            case "updateOddsMode":
                //次要玩法赔率模式调整赔率
                return SpringContextUtils.getBeanByClass(LogOddsModelChangeFormat.class);
            case "updateOddsValue":
                //次要玩法调整赔率
                return SpringContextUtils.getBeanByClass(LogSubOddsChangeFormat.class);
            case "updateMarketOddsValue":
                //調整賠率
                return SpringContextUtils.getBeanByClass(LogOddsChangeFormat.class);
            case "modifyPlayOddsConfig":
                //切换数据源
                return SpringContextUtils.getBeanByClass(LogDataSourceChangeFormat.class);
            case "updateMatchMarketConfig":
                //調價窗口參數調整
                return SpringContextUtils.getBeanByClass(LogUpdateMarketConfigFormat.class);
            case "marketDisable":
                //設置-盘口弃用
                return SpringContextUtils.getBeanByClass(LogMarketDisableFormat.class);
            case "updateMarketTradeType":
                //切换操盘模式
                return SpringContextUtils.getBeanByClass(LogUpdateMarketTradeTypeFormat.class);
            case "updateMatchMarketValue":
                //新增/調整盤口
                return SpringContextUtils.getBeanByClass(LogUpdateMarketValueFormat.class);
            case "updateTournamentLevel":
                //聯賽模板-联赛属性
                return SpringContextUtils.getBeanByClass(LogUpdateTournamentLevelFormat.class);
            case "updateTournamentTemplate":
                //联赛模板日志需求-模板选择
                return SpringContextUtils.getBeanByClass(LogTemplateSelectionFormat.class);
            case "removeSpecialTemplate":
                //联赛模板日志需求-模板删除
                return SpringContextUtils.getBeanByClass(LogTemplateDeleteFormat.class);
            case "update":
                //联赛模板日志需求-模板修改
                return SpringContextUtils.getBeanByClass(LogTemplateUpdateFormat.class);
            case "updatePlayOddsConfig":
                //聯賽模板-玩法賠率源設置
                return SpringContextUtils.getBeanByClass(LogUpdatePlayOddsConfigFormat.class);
            case "modifyBaiJiaConfig":
                //更新赛事百家赔数据
                return SpringContextUtils.getBeanByClass(LogModifyBaiJiaConfigFormat.class);
            case "modifyMargainRef":
                //分時節點-调整操盘参数
                return SpringContextUtils.getBeanByClass(LogModifyMargainRefFormat.class);
            case "removeMargainRef":
                //分時節點-模板刪除
                return SpringContextUtils.getBeanByClass(LogRemoveMargainRefFormat.class);
            case "updateMarketWater":
                //調水差 獨贏玩法
                return SpringContextUtils.getBeanByClass(LogUpdateMarketWaterFormat.class);
            case "matchChangeStatusSource":
                //切数据源-赛事状态源
                return SpringContextUtils.getBeanByClass(LogChangeStatusSourceFormat.class);
            case "matchChangeEventSource":
                //切数据源-实时事件源
                return SpringContextUtils.getBeanByClass(LogChangeEventSourceFormat.class);
            case "updateMarketStatus":
                //修改操盘状态
                return SpringContextUtils.getBeanByClass(LogUpdateMarketStatusFormat.class);
            case "modifyMatchPayVal":
                //操盤設置-商戶/用戶 單場賠付限額
                return SpringContextUtils.getBeanByClass(LogModifyMatchPayValFormat.class);
            case "modifySettleSwitch":
                //操盤設置-提前結算開關
                return SpringContextUtils.getBeanByClass(LogModifySettleSwitchFormat.class);
            case "modifyTemplate":
                //操盤設置-比分源
                return SpringContextUtils.getBeanByClass(LogModifyScoreSourceFormat.class);
            case "modifyTemplateEvent":
                //操盤設置-誰先開球/角球/進球/事件
                return SpringContextUtils.getBeanByClass(LogModifyTemplateEventFormat.class);
            case "modifyPlayMargain":
                //操盤設置-盤口參數調整-最大盤口數/盤口賠付預警/支持串關/賠率(水差)變動幅度/自動關盤時間設置
                return SpringContextUtils.getBeanByClass(LogModifyPlayMargainFormat.class);
            case "modifyMatchTempByLevelTemp":
                //操盤設置-同步联赛模板
                return SpringContextUtils.getBeanByClass(LogModifyMatchTempByLevelTempFormat.class);
            case "modifySpecialInterval":
                //操盤設置-盤口參數調整-特殊抽水
                return SpringContextUtils.getBeanByClass(LogModifySpecialIntervalFormat.class);
            case "updateEventAndTimeConfig":
                //聯賽模板-自动接拒设置-保存
                return SpringContextUtils.getBeanByClass(LogUpdateEventAndTimeConfigFormat.class);
            case "copyEventAndTimeConfig":
                //联赛模板日志-接拒单玩法集事件复制功能
                return SpringContextUtils.getBeanByClass(LogCopyEventAndTimeConfigFormat.class);
            default:
                throw new Exception("操盤日誌-未有對應方法名稱");
        }
    }

}
