package com.wp.casino.messageserver.domain.mysql.account;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author sn
 * @date 2020/5/29 15:37
 */
@Data
@Entity
@Table(name = "tb_account_detail")
public class AccountDetail implements Serializable {

    @Id
    Long aUid;

    String aAccount;

    String aPlat;

    String aNickname;

    Integer aSex;

    Integer aAge;

    String aFace;

    String aDesc;

    String aRealname;

    String aAddress;

    String aCode;

    String aPhone;
    Integer aRegTime;

    Integer aChannel;

    String aRegPn;

    Integer aRegGameid;

    String aRegImei;

    Integer aLastLoginTime;

    Integer aTimestamp;


    Integer aStatus;

    Integer aForbidTime;


}
