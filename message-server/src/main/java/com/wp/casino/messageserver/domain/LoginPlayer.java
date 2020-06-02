package com.wp.casino.messageserver.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册在message上的用户
 */
@Data
public class LoginPlayer implements Serializable {

    private long plyGuid;

    private String nickName;

    private Integer plyVip;

    private Integer plyLevel;

    private String headImg;

    private Integer userLanguage;

    private String serverId;
}
