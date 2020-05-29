package com.wp.casino.messageserver.domain.mysql.casino;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.print.DocFlavor;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/29 18:41
 */
@Data
@Entity
@Table(name = "tb_message_friend_list")
public class MessageFriendList implements Serializable {



    @Id
    Long ml_ply_guid;

    Long ml_friend_guid;

    String ml_remark_name;

    String ml_add_time;

    String ml_last_msg;

    Integer ml_last_msg_time;

    String ml_max_read_msg_id;

    Integer ml_unread_num;

    Integer ml_flag;



}
