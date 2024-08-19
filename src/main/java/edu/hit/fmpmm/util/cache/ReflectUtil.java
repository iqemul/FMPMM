package edu.hit.fmpmm.util.cache;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtil<T> {
    public Map<String, Object> object2Map(T object) {
        Map<String, Object> objectMap = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            // 通过setAccessible()设置为true,允许通过反射访问私有变量
            field.setAccessible(true);
            try {
                String name = field.getName();  // 获取属性名
                Object value = field.get(object);  // 获取属性值
                if (value == null) {
                    value = "";
                }
                objectMap.put(name, value.toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return objectMap;
    }

    public T map2Object(Map<Object, Object> map, Class<T> clazz)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        T object = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();  // 获取所有字段
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = map.get(field.getName());
            field.set(object, value);  // 设置字段值
        }
        return object;
    }
}
