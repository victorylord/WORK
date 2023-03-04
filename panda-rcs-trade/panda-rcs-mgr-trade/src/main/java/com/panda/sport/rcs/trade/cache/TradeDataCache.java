package com.panda.sport.rcs.trade.cache;

import com.panda.sport.rcs.pojo.dto.TradeCacheDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wealth
 * @version V1.0
 * @Description 操盘相关本地缓存类处理
 * @date 2022-09-25 15:00:00
 */
@Slf4j
@Component
public class TradeDataCache {

    /**
     * 数据存储过期时间设置为3小时
     */
    private final static Long EXPIRED_TIME = 3 * 60 * 60 * 1000L;

    /**
     * 赛事列表信息
     * tournamentJsonString
     */
    public static Map<String, TradeCacheDataDTO> dangerTournamentMap = new ConcurrentHashMap<>(4096);

    /**
     * 赛事列表信息
     * teamJsonString
     */
    public static Map<String, TradeCacheDataDTO> dangerTeamMap = new ConcurrentHashMap<>(4096);

    /**
     * 获取缓存数据
     *
     * @param cacheKey
     * @return
     */
    public static String getDangerTournamentMap(String cacheKey){
        if(!dangerTournamentMap.containsKey(cacheKey)){
            return null;
        }
        return dangerTournamentMap.get(cacheKey).getCacheValue();
    }

    /**
     * 获取缓存数据
     *
     * @param cacheKey
     * @return
     */
    public static String getDangerTeamMap(String cacheKey){
        if(!dangerTeamMap.containsKey(cacheKey)){
            return null;
        }
        return dangerTeamMap.get(cacheKey).getCacheValue();
    }


    /**
     * 本地缓存数据检查，创建时间超过3个小时，自动清理
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void checkDangerData(){
        Long currTime = System.currentTimeMillis();

        if(dangerTournamentMap.size() > 0){
            log.info("::标准赛事联赛ID::当前集合总数据={}", dangerTournamentMap.size());
            dangerTournamentMap.forEach((k, v) -> {
                if(currTime - v.getCreateTime() > EXPIRED_TIME){
                    dangerTournamentMap.remove(k);
                }
            });
        }

        if(dangerTeamMap.size() > 0){
            log.info("::赛事比赛队伍ID::当前集合总数据={}", dangerTeamMap.size());
            dangerTeamMap.forEach((k, v) -> {
                if(currTime - v.getCreateTime() > EXPIRED_TIME){
                    dangerTeamMap.remove(k);
                }
            });
        }
    }
}
