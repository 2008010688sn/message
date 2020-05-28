package com.wp.casino.messageserver.common;

public enum MsgType {

    // 俱乐部消息
    CLUB_NOTI_MSG(1),
    // 个人消息
    PLAYER_NOTI_MSG(2),
    // 游戏相关消息
    GAME_NOTI_MSG(3);

    private int value;

    private MsgType(int value) {
        this.value = value;
    }

    public int getMsgType() {
        return value;
    }
}
