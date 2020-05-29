package com.wp.casino.messageserver.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.print.DocFlavor;
import java.io.Serializable;

/**
 * 暂留
 * @author sn
 * @date 2020/5/29 10:09
 */
@Data
@Entity
@Table(name = "tb_pyq_club_role_privilege")
public class PyqClubRolePrivilege implements Serializable {

    @Id
    //俱乐部id
    Integer pcAutoid;

    //'角色id'
    Integer pcClubid;

    //'角色id'
    Integer pcRoleid;

    //'角色描述'
    String pcRoleDesc;

    //'权限id集合'
    String pcPrivilegeList;

    //'更新时间'
    Integer pcUpdateTime;

    //'角色等级'
    Integer pcRoleLevel;


}
