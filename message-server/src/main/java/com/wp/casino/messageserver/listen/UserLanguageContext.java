package com.wp.casino.messageserver.listen;

import com.wp.casino.messageserver.common.MsgConstants;
import com.wp.casino.messageserver.domain.mysql.casino.ConfigGlobalString;
import com.wp.casino.messageserver.domain.mysql.casino.GlobalErrorDesc;
import com.wp.casino.messageserver.service.MessageClient;
import com.wp.casino.messageserver.utils.ClubDataUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author sn
 * @date 2020/5/19 17:18
 */
@Component
@Slf4j
@Order(3)
public class UserLanguageContext implements ApplicationRunner {

    public  static ConcurrentHashMap<String, String> enMaps=new ConcurrentHashMap<>(100);
    public  static ConcurrentHashMap<String, String> cnMaps=new ConcurrentHashMap<>(100);
    public  static ConcurrentHashMap<String, String> twMaps=new ConcurrentHashMap<>(100);

    public  static ConcurrentHashMap<Integer, String> enErrMaps=new ConcurrentHashMap<>(100);
    public  static ConcurrentHashMap<Integer, String> cnErrMaps=new ConcurrentHashMap<>(100);
    public  static ConcurrentHashMap<Integer, String> twErrMaps=new ConcurrentHashMap<>(100);


    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<ConfigGlobalString> list =  ClubDataUtil.findConfigGlobalList();
        List<GlobalErrorDesc> errlist =  ClubDataUtil.findGlobalErrorDescList();
        if (list != null && list.size() > 0) {
            enMaps = (ConcurrentHashMap<String, String>) list.stream().filter(cgs ->  MsgConstants.EN_US_LANGUAGE==cgs.getGsLang()).collect(Collectors.toConcurrentMap(ConfigGlobalString::getGsName, ConfigGlobalString::getGsContext));
            cnMaps = (ConcurrentHashMap<String, String>) list.stream().filter(cgs ->  MsgConstants.ZH_CN_LANGUAGE==cgs.getGsLang()).collect(Collectors.toConcurrentMap(ConfigGlobalString::getGsName, ConfigGlobalString::getGsContext));
            twMaps = (ConcurrentHashMap<String, String>) list.stream().filter(cgs ->  MsgConstants.ZH_TW_LANGUAGE==cgs.getGsLang()).collect(Collectors.toConcurrentMap(ConfigGlobalString::getGsName, ConfigGlobalString::getGsContext));

//            for (ConfigGlobalString cgs : list) {
//                switch (cgs.getGsLang()) {
//                    case MsgConstants.EN_US_LANGUAGE :
//                        enMaps.put(cgs.getGsName(), cgs.getGsContext());
//                    case MsgConstants.ZH_CN_LANGUAGE :
//                        cnMaps.put(cgs.getGsName(), cgs.getGsContext());
//                    case MsgConstants.ZH_TW_LANGUAGE:
//                        twMaps.put(cgs.getGsName(), cgs.getGsContext());
//                    default:
//                        log.warn("UserLanguageContext-----no target language");
//                        enMaps.put(cgs.getGsName(),cgs.getGsContext());
//                }
//            }
        }

        if (errlist != null && errlist.size() > 0) {
            for (GlobalErrorDesc gd : errlist) {
                enErrMaps.put(gd.getGdAutoId(), gd.getGdEnUsDesc());
                cnErrMaps.put(gd.getGdAutoId(), gd.getGdZhCnDesc());
                twErrMaps.put(gd.getGdAutoId(), gd.getGdZhTwDesc());
            }
        }

        log.info("----");
    }
}
