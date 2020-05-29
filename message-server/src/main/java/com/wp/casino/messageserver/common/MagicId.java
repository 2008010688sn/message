package com.wp.casino.messageserver.common;

public enum MagicId {

    APPLY_JOIN_ROOM_MSG("apply_join_room_msg"),
    AGREE_JOIN_TABLE_MSG("agree_join_table_msg"),
    REFUSE_JOIN_TABLE_MSG("refuse_join_table_msg"),

    APPLY_JOIN_CLUB_MSG("apply_join_club_msg"),
    DISMISS_CLUB_MSG("dismiss_club_msg"),// 解散俱乐部
    DROP_OUT_CLUB_MSG("drop_out_club_msg"),
    AGREE_JOIN_CLUB_MSG("agree_join_club_msg"),
    REFUSE_JOIN_CLUB_MSG("refuse_join_club_msg"),
    KICK_OUT_CLUB_MSG("kick_out_club_msg");

    private final String magicId;

    private MagicId(String magicId)
    {
        this.magicId = magicId;
    }

    public String getMagicId() {
        return magicId;
    }
}
