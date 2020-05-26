package com.wp.casino.messageserver.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/20 19:58
 */
@Data
public class MessageContext implements Serializable {

    private String content;

    private String text;



}
