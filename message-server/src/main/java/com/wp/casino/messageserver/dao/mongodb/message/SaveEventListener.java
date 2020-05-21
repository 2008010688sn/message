package com.wp.casino.messageserver.dao.mongodb.message;


import com.wp.casino.messageserver.common.AutoValue;
import com.wp.casino.messageserver.common.SeqInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;


/**
 * 保存文档监听类<br>
 * 在保存对象时，通过反射方式为其生成ID
 *
 */

/**
 * @Document  把一个java类声明为mongodb的文档，可以通过collection参数指定这个类对应的文档
 *
 */
@Component
@Slf4j
public class SaveEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    @Qualifier("messageMongoTemplate")
    protected MongoTemplate mongoTemplate;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        final Object source = event.getSource();
        if (source != null) {
            ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);
                    // 如果字段添加了我们自定义的AutoValue注解
                    if (field.isAnnotationPresent(AutoValue.class) /*&& field.get(source) instanceof Number*/
                          /*  && field.getLong(source) == 0*/) {
                        // field.get(source) instanceof Number &&
                        // field.getLong(source)==0
                        // 判断注解的字段是否为number类型且值是否等于0.如果大于0说明有ID不需要生成ID
                        // 设置自增ID
                        field.set(source, getNextId(source.getClass().getSimpleName()));

                       /* AutoValue autoValue=field.getAnnotation(AutoValue.class);
                        long defaultValue=0;
                        if (autoValue!=null){
                             defaultValue=autoValue.value();
                        }*/

//                        field.set(source, getNextId(defaultValue,source.getClass().getSimpleName()));
                        log.debug("集合的ID为======================="+ source);
                    }
                }
            });
        }
    }

    /**
     * 获取下一个自增ID
     *
     * @param collName
     *            集合（这里用类名，就唯一性来说最好还是存放长类名）名称
     * @return 序列值
     */
    public Long getNextId(String collName) {
        log.debug("CollectionsName======================="+collName);
        Query query = new Query(Criteria.where("collName").is(collName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);

        SeqInfo seq = this.mongoTemplate.findAndModify(query, update, options, SeqInfo.class);
        log.debug(collName+"集合的ID为======================="+seq.getSeqId());
        return seq.getSeqId();
    }


}