package com.wp.casino.messagetools.monitor;

import com.google.common.collect.Maps;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * @author sn
 * @date 2020/6/9 16:48
 */
public class JVMInfo implements InfoQuota {
    private RuntimeMXBean runtimeMXBean;

    private OperatingSystemMXBean systemMXBean;

    private String currentPid;

    public JVMInfo() {
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        systemMXBean = ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public String pid() {
        if (null == currentPid) {
            currentPid = runtimeMXBean.getName().split("@")[0];
        }
        return currentPid;
    }

    @Override
    public double load() {
        double averageLoad = systemMXBean.getSystemLoadAverage();
        return averageLoad;
    }

    @Override
    public Object monitor(Object... args) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("pid", pid());
        map.put("load", load());
        map.put("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + "m");
        map.put("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + "m");
        map.put("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + "m");
        return map;
    }
}
