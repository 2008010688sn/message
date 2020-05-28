package com.wp.casino.messageserver.domain;

import lombok.Data;
import org.omg.CORBA.LongLongSeqHelper;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/28 12:19
 */
@Data
@Entity
@Table(name = "tb_pyq_club_members")
public class PyqClubMembers implements Serializable {

    @Id
    Integer cmAutoId;

    Integer cmClubId;

    Long   cmPlyGuid;

    Integer cmRole;

    Integer cmJoinTime;

    Integer cmStatus;

    Integer cmPushStatus;

}
