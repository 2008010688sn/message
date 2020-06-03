package com.wp.casino.messageserver.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/20 19:53
 */
@Data
public class ReceiveObj implements Serializable {

    @Field(name = "id")
    private long id;

    private Integer status;

}
