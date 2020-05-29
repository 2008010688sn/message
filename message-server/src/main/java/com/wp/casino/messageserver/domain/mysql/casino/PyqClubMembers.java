package com.wp.casino.messageserver.domain.mysql.casino;

import lombok.Data;
import lombok.Generated;
import javax.persistence.*;
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
    @GeneratedValue(strategy= GenerationType.IDENTITY) // MYSQL时可以这样使用自增
    Integer cmAutoId;

    Integer cmClubId;

    Long   cmPlyGuid;

    Integer cmRole;

    Integer cmJoinTime;

    Integer cmStatus;

    Integer cmPushStatus;

}
