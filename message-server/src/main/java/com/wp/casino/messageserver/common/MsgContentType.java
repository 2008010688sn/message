package com.wp.casino.messageserver.common;

public enum MsgContentType {

    // 文本类型
    TEXT_MSG(1),
    // 可操作类型
    ACK_MSG(2);

    private int value;

    private MsgContentType(int value) {
        this.value = value;
    }

    public int getMsgContentType() {
        return value;
    }
}
