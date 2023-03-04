//package com.panda.sport.rcs.trade.util;
//
//import com.panda.sport.rcs.core.cache.properties.RedisProperties;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.Redisson;
//import org.redisson.api.RLock;
//import org.redisson.config.Config;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.TimeUnit;
//
//@Component
//@Slf4j
//public class RedissonManager {  
//  
//    private Config config = new Config();  
//    private Redisson redisson = null;  
//
//   public RedissonManager(RedisProperties redisProperties){  
//        try {
//            config.useClusterServers() //这是用的集群server 
//                    .setScanInterval(2000) //设置集群状态扫描时间 
//                    .setMasterConnectionPoolSize(10000) //设置连接数 
//                    .setSlaveConnectionPoolSize(10000) 
//                    .setIdleConnectionTimeout(1000 * 60 * 5)
//                    .setPassword(redisProperties.getPassword())
//                    .addNodeAddress(redisProperties.getNodesString().split(","));  
//            redisson = (Redisson) Redisson.create(config);
//        }catch (Exception e){  
//            log.error(e.getMessage(),e);
//        }  
//    }  
//  
//    public Redisson getRedisson(){  
//        return redisson;  
//    }  
//    
//    private static final String LOCK_TITLE = "RCS:lock:";  
//    
//    public boolean lock(String lockName,int timeOut){  
//        String key = LOCK_TITLE + lockName;  
//        RLock mylock = redisson.getLock(key);  
//        mylock.lock(timeOut, TimeUnit.SECONDS); //lock提供带timeout参数，timeout结束强制解锁，防止死锁  
//        return  true;  
//    } 
//  
//    public boolean lock(String lockName){  
//    	return lock(lockName, 10);
//    }  
//  
//    public void unlock(String lockName){  
//        String key = LOCK_TITLE + lockName;  
//        RLock mylock = redisson.getLock(key);  
//        mylock.unlock();  
//    }  
//  
//}
