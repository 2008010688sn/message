package com.wp.casino.messageapi.service;

/**
 * @author sn
 * @date 2020/5/15 14:54
 */
public interface Listener {
    void onSuccess(Object... args);

    void onFailure(Throwable cause);
}
