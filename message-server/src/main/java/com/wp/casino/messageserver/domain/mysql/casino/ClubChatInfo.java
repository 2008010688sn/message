package com.wp.casino.messageserver.domain.mysql.casino;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/29 18:20
 */
@Data
@Entity
@Table(name = "tb_pyq_club_chat_info")
public class ClubChatInfo implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Integer clAutoId;

    Integer clClubMessageId;

    Long clMemberUid;

    Integer clGameId;

    Integer clClubId;

    String clChatMessage;

    Integer clMessageSendTime;

    Integer clMsgType;



}
