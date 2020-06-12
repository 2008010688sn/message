package com.wp.casino.messagetools.monitor;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author sn
 * @date 2020/6/9 17:06
 */
@Data
public class ResultCollector {

    private final JVMInfo jvmInfo;
    private final JVMGC jvmgc;
    private final JVMMemory jvmMemory;
    private final JVMThread jvmThread;
    private final  JVMThreadPool jvmThreadPool;

    public ResultCollector() {
        this.jvmInfo = new JVMInfo();
        this.jvmgc = new JVMGC();
        this.jvmMemory = new JVMMemory();
        this.jvmThread = new JVMThread();
        this.jvmThreadPool = new JVMThreadPool();
    }

    public MonitorResult collect() {
        MonitorResult result = new MonitorResult();
        result.addResult("jvm-info", jvmInfo.monitor());
        result.addResult("jvm-gc", jvmgc.monitor());
        result.addResult("jvm-memory", jvmMemory.monitor());
        result.addResult("jvm-thread", jvmThread.monitor());
        result.addResult("jvm-thread-pool", jvmThreadPool.monitor());
        return result;
    }




}
