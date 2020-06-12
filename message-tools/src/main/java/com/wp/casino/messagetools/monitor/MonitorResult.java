package com.wp.casino.messagetools.monitor;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sn
 * @date 2020/6/9 17:07
 */
public class MonitorResult {

    private Long timestamp = System.currentTimeMillis();
    private Map<String, Object> results = new HashMap<>(8);

    public MonitorResult addResult(String name, Object result) {
        results.put(name, result);
        return this;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public MonitorResult setResults(Map<String, Object> results) {
        this.results = results;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public MonitorResult setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "MonitorResult{" +
                "results=" + results +
                ", timestamp=" + timestamp +
                '}';
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }

}
