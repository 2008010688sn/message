package com.wp.casino.messageserver.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/20 19:53
 */
@Data
public class ClubMsgCount implements Serializable {

    private int clubId;

    private int count;

}
