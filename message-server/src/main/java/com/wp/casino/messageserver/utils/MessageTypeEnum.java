package com.wp.casino.messageserver.utils;

import lombok.Data;

/**
 * @author sn
 * @date 2020/6/3 16:02
 */

public enum MessageTypeEnum {

    CLUB_MSG(1,"CLUB MESSAGE"),
    PERSON_MSG(2,"PERSON MESSAGE"),
    GAME_MSG(3,"GAME MESSAGE");


    private final int value;

    private final String desc;

    MessageTypeEnum(int value, String desc) {
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
