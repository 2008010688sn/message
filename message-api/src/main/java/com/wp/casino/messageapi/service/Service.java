
package com.wp.casino.messageapi.service;

import java.util.concurrent.CompletableFuture;

/**
 * @author sn
 * @date 2020/5/15 14:54
 */
public interface Service {

    void start(Listener listener);

    void stop(Listener listener);

    CompletableFuture<Boolean> start();

    CompletableFuture<Boolean> stop();

    boolean syncStart();

    boolean syncStop();

    void init();

    boolean isRunning();

}
