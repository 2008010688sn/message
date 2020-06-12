package com.wp.casino.messagetools.monitor;

/**
 * @author sn
 * @date 2020/6/9 16:50
 */
public interface ThreadQuota  extends MonitorQuota {

    int daemonThreadCount();

    int threadCount();

    long totalStartedThreadCount();

    int deadLockedThreadCount();

}
