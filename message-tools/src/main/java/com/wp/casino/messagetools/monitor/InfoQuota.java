package com.wp.casino.messagetools.monitor;

/**
 * @author sn
 * @date 2020/6/9 16:47
 */
public interface InfoQuota extends MonitorQuota {
    String pid();

    double load();
}
