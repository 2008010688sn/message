package com.wp.casino.messageserver.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * apply_join_room_msg
 */
@Data
public class RoomMessageContext implements Serializable {

    private Integer tableid;

    private String tablename;

    private Integer tablecreatetime;

    private Integer serverid;

    private String plynickname;

    private long plyguid;

    private String magic_id;

    private Integer invitecode;

    private Integer gameid;

    private String content;

    private Integer code;

    private String text;
}
