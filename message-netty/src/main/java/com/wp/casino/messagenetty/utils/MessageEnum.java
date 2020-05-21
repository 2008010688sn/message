package com.wp.casino.messagenetty.utils;

/**
 * 对象枚举
 */
public enum  MessageEnum {

    BDOVS_WW_USER_DATA_CHANGE_REQ(7130,"com.wp.casino.messagenetty.proto","PBCSMessage","proto_ww_user_data_change_req"),
    WW_FRIEND_MSG_REQ(7125,"com.wp.casino.messagenetty.proto","PBCSMessage","proto_ww_friend_msg_req"),
    LM_REGISTER_REQ(1000,"com.wp.casino.messagenetty.proto","PBCSMessage","proto_lm_register_req"),
    ML_REGISTER_ACK(1000,"com.wp.casino.messagenetty.proto","PBCSMessage","proto_ml_register_ack"),
    LM_UPDATE_PLY_LOGIN_STATUS_NOT(1001,"com.wp.casino.messagenetty.proto","PBCSMessage","proto_lm_update_ply_login_status_not"),
    LM_NOTI_MSG(1002,"com.wp.casino.messagenetty.proto","PBCSMessage","proto_lm_noti_msg"),
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
