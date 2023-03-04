package com.panda.sport.rcs.trade.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 线程池配置
 *
 * @author lithan
 */
public final class ThreadUtil {
    //线程数量
    private static final int THREAD_NUM = 100;
    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("threadName-%d").build();
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), threadFactory);

    public static void submit(Runnable task) {
        threadPool.execute(task);
    }

    /**
     * 多线程执行列表任务 并返回自定义结果
     *
     * @param list
     * @param execute
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> executeFutures(List<T> list, Function<T, R> execute) {
        if (execute == null || CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<CompletableFuture<R>> executeFutures =
                list.parallelStream()
                        .map(e -> CompletableFuture.supplyAsync(() -> execute.apply(e)
                                , threadPool))
                        .collect(Collectors.toList());
        return executeFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public static void destory() {
        threadPool.shutdown();
    }

    public static Integer size() {
        return threadPool.getQueue().size();
    }
}
