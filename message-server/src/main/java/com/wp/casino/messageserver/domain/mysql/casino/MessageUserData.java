package com.wp.casino.messageserver.domain.mysql.casino;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/29 15:52
 */
@Entity
@Table(name = "tb_message_user_data")
@Data
public class MessageUserData implements Serializable {

    @Id
    Long mdPlyGuid;

    String mdNickname;

    Integer mdVip;

    Integer mdLevel;

    Integer mdFriendLimit;

    Integer mdApproveNoti;

    Integer mdLoginTime;

    Integer mdSystemAutoId;



}
