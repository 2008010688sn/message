package com.wp.casino.messagetools.monitor;

/**
 * @author sn
 * @date 2020/6/9 16:40
 */
public interface GCMQuota extends MonitorQuota {

    long yongGcCollectionCount();

    long yongGcCollectionTime();

    long fullGcCollectionCount();

    long fullGcCollectionTime();

    long spanYongGcCollectionCount();

    long spanYongGcCollectionTime();

    long spanFullGcCollectionCount();

    long spanFullGcCollectionTime();

}
