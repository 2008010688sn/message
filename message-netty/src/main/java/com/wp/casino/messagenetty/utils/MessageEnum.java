package com.wp.casino.messagenetty.utils;

/**
 * 对象枚举
 */
public enum  MessageEnum {
//worldMessage
    FW_REGISTER_REQ(7180,"com.wp.casino.messagenetty.proto","WorldMessage","proto_fw_register_req"),
    WF_REGISTER_ACK(7181,"com.wp.casino.messagenetty.proto","WorldMessage","proto_wf_register_ack"),
    WF_SYSTEM_CHAT_REQ(7183,"com.wp.casino.messagenetty.proto","WorldMessage","proto_wf_system_chat_req"),
    WF_WEB_MSG_NOTI(20538,"com.wp.casino.messagenetty.proto","WorldMessage","proto_wf_web_msg_noti"),
    WL_NOTI_MSG_DATA(20539,"com.wp.casino.messagenetty.proto","WorldMessage","proto_wl_noti_msg_data"),
    WF_JOIN_ROOM_NOTI(20529,"com.wp.casino.messagenetty.proto","WorldMessage","proto_wf_join_room_noti"),
    WF_BREAK_UP_CLUB_NOTI(20540,"com.wp.casino.messagenetty.proto","WorldMessage","proto_wf_break_up_club_noti"),

    WF_CLUB_MEMBER_UPDATE_NOTI(20541,"com.wp.casino.messagenetty.proto","WorldMessage","proto_wf_club_member_update_noti"),
    PING(7200,"com.wp.casino.messagenetty.proto","WorldMessage","prt_ping"),
    PONG(7201,"com.wp.casino.messagenetty.proto","WorldMessage","prt_pong"),

//loginMessage

    LC_CLUB_APPLY_JOIN_ACK(20149,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lc_club_apply_join_ack"),
    LF_CLUB_APPLY_JOIN_NOTI(20150,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lf_club_apply_join_noti"),
    LF_JOIN_ROOM_REPLY_NOTI(20161,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lf_join_room_reply_noti"),

    CL_LOAD_NOTI_MSG_REQ(20163,"com.wp.casino.messagenetty.proto","LoginMessage","proto_cl_load_noti_msg_req"),
    LC_LOAD_NOTI_MSG_ACK(20164,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lc_load_noti_msg_ack"),

    CL_UPDATE_MSG_STATUS_REQ(20179,"com.wp.casino.messagenetty.proto","LoginMessage","proto_cl_update_msg_status_req"),
    LC_UPDATE_MSG_STATUS_ACK(20180,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lc_update_msg_status_ack"),

    CL_GET_MSG_COUNT_REQ(20181,"com.wp.casino.messagenetty.proto","LoginMessage","proto_cl_get_msg_count_req"),
    LC_GET_MSG_COUNT_ACK(20182,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lc_get_msg_count_ack"),

    CF_ADD_CLUB_CHAT_RECORD_REQ(22026,"com.wp.casino.messagenetty.proto","LoginMessage","proto_cf_add_club_chat_record_req"),
    FC_ADD_CLUB_CHAT_RECORD_ACK(22027,"com.wp.casino.messagenetty.proto","LoginMessage","proto_fc_add_club_chat_record_ack"),
    FC_ADD_CLUB_CHAT_RECORD_NOTI(22028,"com.wp.casino.messagenetty.proto","LoginMessage","proto_fc_add_club_chat_record_noti"),


    CF_SYNC_CLUB_CHAT_RECORD_REQ(22029,"com.wp.casino.messagenetty.proto","LoginMessage","proto_cf_sync_club_chat_record_req"),
    FC_SYNC_CLUB_CHAT_RECORD_ACK(22030,"com.wp.casino.messagenetty.proto","LoginMessage","proto_fc_sync_club_chat_record_ack"),

    CF_MESSAGE_WRAP_SYNC(22031,"com.wp.casino.messagenetty.proto","LoginMessage","proto_cf_message_wrap_sync"),
    FC_MESSAGE_WRAP_SYNC(22032,"com.wp.casino.messagenetty.proto","LoginMessage","proto_fc_message_wrap_sync"),

    FL_CLUB_NOTIFY(22033,"com.wp.casino.messagenetty.proto","LoginMessage","proto_fl_club_notify"),


    LF_REGISTER_REQ(22000,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lf_register_req"),
    FL_REGISTER_ACK(22001,"com.wp.casino.messagenetty.proto","LoginMessage","proto_fl_register_ack"),

    LF_UPDATE_PLY_LOGIN_STATUS_NOT(22002,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lf_update_ply_login_status_not"),
    LF_UPDATE_PLY_LOGOUT_STATUS_NOT(22003,"com.wp.casino.messagenetty.proto","LoginMessage","proto_lf_update_ply_logout_status_not"),

    FL_NOTI_MSG(20162,"com.wp.casino.messagenetty.proto","LoginMessage","proto_fl_noti_msg"),

    ;
    /**
     * 消息id，必须唯一
     */
    private final int opCode;
    /**
     * java包名
     */
    private final String javaPackageName;

    /**
     * java外部类名字
     */
    private final String javaOuterClassName;

    /**
     * 消息名(类简单名)
     * {@link Class#getSimpleName()}
     */
    private final String messageName;

    MessageEnum(int opCode, String javaPackageName, String javaOuterClassName, String messageName) {
        this.opCode = opCode;
        this.javaPackageName = javaPackageName;
        this.javaOuterClassName = javaOuterClassName;
        this.messageName = messageName;
    }

    public int getOpCode() {
        return opCode;
    }

    public String getJavaPackageName() {
        return javaPackageName;
    }

    public String getJavaOuterClassName() {
        return javaOuterClassName;
    }

    public String getMessageName() {
        return messageName;
    }

    @Override
    public String toString() {
        return "MessageEnum{" +
                "opCode=" + opCode +
                ", javaPackageName='" + javaPackageName + '\'' +
                ", javaOuterClassName='" + javaOuterClassName + '\'' +
                ", messageName='" + messageName + '\'' +
                '}';
    }
}
