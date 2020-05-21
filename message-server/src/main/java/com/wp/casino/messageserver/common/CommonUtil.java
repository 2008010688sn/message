package com.wp.casino.messageserver.common;

import com.mongodb.DBObject;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/*
* 类描述：
*
*/
public class CommonUtil {

    /**
     * 将首字母变小写
     * @param str
     * @return
     */
    public static String toFirstCharLowerCase(String str){
        char[]  columnCharArr = str.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < columnCharArr.length; i++) {
            char cur = columnCharArr[i];
            if(i==0){
                sb.append(Character.toLowerCase(cur));
            }else{
                sb.append(cur);
            }
        }
        return sb.toString();
    }

    /**
     * dbobject转成对象
     * @param dbObject
     * @param bean
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static <T> T dbObject2Bean(DBObject dbObject, T bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (bean == null) {  //测试已通过
            return null;
        }
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            String varName = field.getName();
            Object object = dbObject.get(varName);
            if (object != null) {
                BeanUtils.setProperty(bean, varName, object);
            }
        }
        return bean;
    }

}
