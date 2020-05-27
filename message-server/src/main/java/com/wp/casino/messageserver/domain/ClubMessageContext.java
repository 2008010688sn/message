package com.wp.casino.messageserver.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/20 19:58
 */
@Data
public class ClubMessageContext implements Serializable {

    private Integer clubId;

    private String clubName;

    private Integer code;

    private String content;

    private String nickName;

    private String plyId;

    private String text;
}
