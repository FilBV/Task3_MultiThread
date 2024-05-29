package ru.inno.tasks;
import java.lang.reflect.Proxy;

public class Utility {

    public static <T> T Cache(T objectIncome) {
        T proxyObject = (T) Proxy.newProxyInstance(
                objectIncome.getClass().getClassLoader(),
                objectIncome.getClass().getInterfaces(),
                new CachingHandler(objectIncome));
        return proxyObject;
    }
}