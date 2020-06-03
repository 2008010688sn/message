package com.wp.casino.messageserver.dao.mysql.casino;

import com.wp.casino.messageserver.domain.mysql.casino.ConfigGlobalString;
import com.wp.casino.messageserver.domain.mysql.casino.GlobalErrorDesc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author sn
 * @date 2020/6/1 15:47
 */
public interface GlobalErrorDescRepository extends JpaRepository<GlobalErrorDesc,Integer> {

    @Query(value = "select gd_auto_id,CONCAT(gd_en_us_desc ,\"(\",CONCAT(gd_auto_id,\"\"),\")\"),CONCAT(gd_zh_cn_desc ,\"(\",CONCAT(gd_auto_id,\"\"),\")\"),CONCAT(gd_zh_tw_desc ,\"(\",CONCAT(gd_auto_id,\"\"),\")\") from tb_global_error_desc",nativeQuery = true)
    List<GlobalErrorDesc> findGlobalErrorDescList();

}
