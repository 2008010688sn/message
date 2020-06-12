package com.wp.casino.messagetools.monitor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ThreadProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author sn
 * @date 2020/6/11 9:38
 */
public class JVMThreadPool implements ThreadPoolQuota {

//    private  ThreadPoolManager threadPoolManager;

//    public JVMThreadPool(ThreadPoolManager threadPoolManager) {
//        this.threadPoolManager = threadPoolManager;
//    }

    public JVMThreadPool() {
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Executor> pools = ThreadPoolManager.getActivePools();
        for (Map.Entry<String, Executor> entry : pools.entrySet()) {
            String serviceName = entry.getKey();
            Executor executor = entry.getValue();
            if (executor instanceof ThreadPoolExecutor) {
                result.put(serviceName, getPoolInfo((ThreadPoolExecutor) executor));
            } else if (executor instanceof EventLoopGroup) {
                result.put(serviceName, getPoolInfo((EventLoopGroup) executor));
            }
        }
        return result;
    }

    public static Map<String, Object> getPoolInfo(ThreadPoolExecutor executor) {
        Map<String, Object> info = new HashMap<>(5);
        info.put("corePoolSize", executor.getCorePoolSize());
        info.put("maxPoolSize", executor.getMaximumPoolSize());
        info.put("activeCount(workingThread)", executor.getActiveCount());
        info.put("poolSize(workThread)", executor.getPoolSize());
        info.put("queueSize(blockedTask)", executor.getQueue().size());
        return info;
    }

    public static Map<String, Object> getPoolInfo(EventLoopGroup executors) {
        Map<String, Object> info = new HashMap<>(3);
        int poolSize = 0, queueSize = 0, activeCount = 0;
        for (EventExecutor e : executors) {
            poolSize++;
            if (e instanceof SingleThreadEventLoop) {
                SingleThreadEventLoop executor = (SingleThreadEventLoop) e;
                queueSize += executor.pendingTasks();
                ThreadProperties tp = executor.threadProperties();
                if (tp.state() == Thread.State.RUNNABLE) {
                    activeCount++;
                }
            }
        }
        info.put("poolSize(workThread)", poolSize);
        info.put("activeCount(workingThread)", activeCount);
        info.put("queueSize(blockedTask)", queueSize);
        return info;
    }

}
