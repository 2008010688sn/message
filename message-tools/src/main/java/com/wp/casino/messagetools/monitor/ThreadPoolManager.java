package com.wp.casino.messagetools.monitor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author sn
 * @date 2020/6/11 9:36
 */
public class ThreadPoolManager  {

    private static final Map<String, Executor> pools=new ConcurrentHashMap<>();

    public void register(String name, Executor executor) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(executor);
        pools.put(name, executor);
    }

    public static Map<String, Executor> getActivePools() {
        return pools;
    }


}
