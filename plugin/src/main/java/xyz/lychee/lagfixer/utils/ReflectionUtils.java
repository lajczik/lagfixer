package xyz.lychee.lagfixer.utils;

import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;

public final class ReflectionUtils {
    public static Object getPrivateField(Class<?> clazz, Object obj, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object ret = field.get(obj);
        field.setAccessible(false);
        return ret;
    }

    public static Field getClassPrivateField(Class<?> clazz, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static Object invokePrivateMethod(Class<?> clazz, Object obj, String methodName) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return ReflectionUtils.invokePrivateMethod(clazz, obj, methodName, new Class[0]);
    }

    public static Object invokePrivateMethod(Class<?> clazz, Object obj, String methodName, Class<?>[] params, Object... args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = ReflectionUtils.getPrivateMethod(clazz, methodName, params);
        return method.invoke(obj, args);
    }

    public static Method getPrivateMethod(Class<?> clazz, String methodName, Class<?>... params) throws NoSuchMethodException, SecurityException {
        Method method = clazz.getDeclaredMethod(methodName, params);
        method.setAccessible(true);
        return method;
    }

    public static Field getPrivateField(Class<?> clazz, Class<?> type) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType() != type) continue;
            f.setAccessible(true);
            return f;
        }
        return null;
    }

    public static <T> T createInstance(String name, AbstractModule module) {
        String version = SupportManager.getInstance().getNmsVersion();
        if (version == null) return null;

        try {
            Class<?> clazz = Class.forName("xyz.lychee.lagfixer.nms." + version + "." + name);
            return (T) clazz.getConstructor(module.getClass()).newInstance(module);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException ex) {
            return null;
        }
    }

    public static <E extends Enum<E>> void convertEnums(Class<E> enumType, EnumSet<E> enumSet, Collection<String> strings) {
        enumSet.clear();
        for (String str : strings) {
            try {
                E value = Enum.valueOf(enumType, str.toUpperCase());
                enumSet.add(value);
            } catch (IllegalArgumentException ex) {
                LagFixer.getInstance().getLogger().warning("Unknown \"" + enumType.getSimpleName() + "\" enum value: " + str.toUpperCase());
            }
        }
    }
}

