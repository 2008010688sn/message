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

    @Query(value = "select gd_auto_id, CONCAT(convert(unhex(hex(convert(gd_en_us_desc using latin1))) using utf8) ,'(',CONCAT(gd_auto_id),')') gd_en_us_desc, CONCAT(convert(unhex(hex(convert(gd_zh_cn_desc using latin1))) using utf8) ,'(',CONCAT(gd_auto_id),')') gd_zh_cn_desc, CONCAT(convert(unhex(hex(convert(gd_zh_tw_desc using latin1))) using utf8) ,'(',CONCAT(gd_auto_id),')') gd_zh_tw_desc from tb_global_error_desc where gd_type = 9 or gd_type = 10",nativeQuery = true)
    List<GlobalErrorDesc> findGlobalErrorDescList();

}
