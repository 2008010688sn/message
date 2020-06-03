package com.wp.casino.messagenetty.utils;

/**
 * @author sn
 * @date 2020/6/3 16:07
 */
public enum MessageShowTypeEnum {

    TEXT_MSG(1,"TEXT MESSAGE"),
    OPERATION_MSG(2,"OPERATION MESSAGE");


    private final int value;

    private final String desc;

    MessageShowTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
