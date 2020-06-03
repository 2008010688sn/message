package com.wp.casino.messageserver.common;

public interface MsgConstants {

    // 消息发送状态
    public static final int SENDED  = 1;
    public static final int NOT_SEND  = 2;

    // 消息读取状态
    public static final int MSG_STATUS_UNREAD = 0;
    public static final int MSG_STATUS_READ = 1;
    public static final int MSG_STATUS_DELETED = 2;

    //用户语言
    public static final int  EN_US_LANGUAGE = 1033;
    public static final int  ZH_CN_LANGUAGE = 2052;
    public static final int  ZH_TW_LANGUAGE = 1028;

    //俱乐部申请入桌消息有效时间
    public static final int CLUB_REQUEST_JOIN_ROOM_EXPIRE_TIME = 30 * 60;
    public static final int CLUB_REQUEST_APPLY_EXPIRE_TIME = 24 * 60 * 60;

    public static final int MSG_REPLY_CODE_OK = 1;
    public static final int MSG_REPLY_CODE_NO = 0;



}
