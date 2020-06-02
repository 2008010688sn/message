package com.wp.casino.messageserver.domain.mysql.casino;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.rowset.serial.SerialArray;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/6/1 17:25
 */
@Data
@Entity
@Table(name = "tb_message_friend_msg_auto_id")
public class MessageFriendAutoId implements Serializable {

    @Id
    Integer ma_auto_id;

}
