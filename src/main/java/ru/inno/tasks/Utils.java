package ru.inno.tasks;

import java.lang.reflect.Proxy;

public class Utils {
    public static <T> T cache(T objectIncome) {

         return (T) Proxy.newProxyInstance(
                objectIncome.getClass().getClassLoader(),
                objectIncome.getClass().getInterfaces(),
                new CachingHandler<>(objectIncome));
    }

}