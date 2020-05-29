package com.wp.casino.messageserver.dao.mysql.accout;

import com.wp.casino.messageserver.domain.mysql.account.AccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author sn
 * @date 2020/5/29 15:34
 */
public interface AccountDetailRepository extends JpaRepository<AccountDetail,Long> {

}
