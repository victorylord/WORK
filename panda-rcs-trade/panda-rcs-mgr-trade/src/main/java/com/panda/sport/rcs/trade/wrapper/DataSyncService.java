package com.panda.sport.rcs.trade.wrapper;


import java.util.Map;

public interface DataSyncService<T> {
    Map<String, String> receive(T data);
}
