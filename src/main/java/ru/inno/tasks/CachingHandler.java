package ru.inno.tasks;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;


public class CachingHandler implements InvocationHandler {
    volatile private Thread mainThread = Thread.currentThread();
    private final Object targetObject;
    private Boolean changed = true;
    private final Map<Method, Map<Snapshotalble, Object>> methodCachesMap = new HashMap<>();
    private final ConcurrentHashMap<Method, Long> methodCacheLiveMap = new ConcurrentHashMap<>();

    List<String> fieldsExclude = new ArrayList<>();

    public void addMethodCacheLiveMap(Method method, Integer timeliveMeth) {
        long endTime = 0;
        if (timeliveMeth > 0)
            endTime = System.currentTimeMillis() + timeliveMeth;
        methodCacheLiveMap.put(method, endTime);
    }

    private void saveState(boolean changed, Method method, Object resultOfInvocation) throws IllegalAccessException {
        if (changed) {
            Map<Snapshotalble, Object> mapSnap;
            if (methodCachesMap.isEmpty() || !methodCachesMap.containsKey(method)) {
                mapSnap = new HashMap<>();
            } else {
                mapSnap = methodCachesMap.get(method);
            }
            mapSnap.put(new SnapShotField(), resultOfInvocation);
            methodCachesMap.put(method, mapSnap);
        }
    }

    private void clearState(Method method) {
        if (!methodCachesMap.isEmpty()) {
            methodCachesMap.remove(method);
        }
    }

    private Object findResultFromCache(Method method) throws IllegalAccessException {
        Object resultOfInvocation = null;
        if (!methodCachesMap.isEmpty() && methodCachesMap.containsKey(method)) {
            Map<Snapshotalble, Object> mapSnap = methodCachesMap.get(method);
            for (Map.Entry<Snapshotalble, Object> entry : mapSnap.entrySet()) {
                Snapshotalble snapshot = entry.getKey();
                if (snapshot.compareSate())
                    resultOfInvocation = mapSnap.get(snapshot);
            }
        }
        return resultOfInvocation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object resultOfInvocation;
        Method curMethod = targetObject.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (curMethod.isAnnotationPresent(Cache.class)) {
            if (!methodCacheLiveMap.isEmpty() && methodCacheLiveMap.containsKey(method)) {
                resultOfInvocation = findResultFromCache(method);
                if (resultOfInvocation == null) {
                    resultOfInvocation = method.invoke(targetObject, args);
                    saveState(changed, method, resultOfInvocation);
                    System.out.println("Значение вычислено(не нашли среди состояний) = " + (double) resultOfInvocation);
                } else {
                    System.out.println("Вызов из кэша = " + (double) resultOfInvocation);
                }
            } else {
                resultOfInvocation = method.invoke(targetObject, args);
                if (methodCachesMap.isEmpty() || !methodCachesMap.containsKey(method)) {
                    System.out.println("Значение вычислено == " + (double) resultOfInvocation);
                } else {
                    if (methodCacheLiveMap.isEmpty() || !methodCacheLiveMap.containsKey(method)) {
                        clearState(method);
                        System.out.println("Значение вычислено(время хранения в кэш вышло ) = " + (double) resultOfInvocation);
                    } else {
                        System.out.println("Значение вычислено = " + (double) resultOfInvocation);
                    }
                }
                saveState(true, method, resultOfInvocation);
            }
            changed = false;
            addMethodCacheLiveMap(method, curMethod.getAnnotation(Cache.class).value());
        } else {
            SnapShotField curSnapBefore = new SnapShotField();
            resultOfInvocation = method.invoke(targetObject, args);
            SnapShotField curSnapAfter = new SnapShotField();

            if (curMethod.isAnnotationPresent(Mutator.class)) {
                changed = true;
                System.out.println("Вызов метода , помеченного аннотацией Mutator ");
            } else {
                curSnapAfter.fieldsExcludeAdd(curSnapBefore.getFieldsSave());
            }
        }
        return resultOfInvocation;
    }

    private void shutdownExecutor(ExecutorService executorSrv) {
        executorSrv.shutdown();
        try {
            if (!executorSrv.awaitTermination(1000, TimeUnit.MILLISECONDS))
                executorSrv.shutdownNow();
        } catch (InterruptedException ex) {
            executorSrv.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void RunThreadClear() {
        ScheduledExecutorService executorSrv = newSingleThreadScheduledExecutor();
        executorSrv.scheduleWithFixedDelay(() -> {
                    if (mainThread.isAlive())
                        methodCacheLiveMap.entrySet().removeIf(e -> e.getValue() != 0 && e.getValue() <= System.currentTimeMillis());
                    else
                        shutdownExecutor(executorSrv);
                }
                , 0, 1, TimeUnit.MILLISECONDS);

    }

    public CachingHandler(Object targetObject) {
        this.targetObject = targetObject;
        Field[] fields = targetObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAccessible())
                field.setAccessible(true);
            if (field.isAnnotationPresent(NotSaveState.class))
                fieldsExclude.add(field.getName());
        }
        RunThreadClear();
    }

    public class SnapShotField implements Snapshotalble {
        private final Map<String, Object> fieldsSave = new HashMap<>();

        private void fieldsExcludeAdd(Map<String, Object> fieldsCmp) {
            if (!fieldsCmp.equals(fieldsSave))
                for (String keySv : fieldsCmp.keySet()) {
                    Object keySvVal = fieldsCmp.get(keySv);
                    for (String keyCmp : fieldsSave.keySet()) {
                        if (keyCmp.equals(keySv) && !fieldsSave.get(keyCmp).equals(keySvVal)) {
                            if (!fieldsExclude.contains(keyCmp))
                                fieldsExclude.add(keyCmp);
                        }
                    }
                }
        }

        public Map<String, Object> getFieldsSave() {
            return fieldsSave;
        }

        public SnapShotField() throws IllegalAccessException {
            Field[] fields = targetObject.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAccessible())
                    field.setAccessible(true);
                fieldsSave.put(field.getName(), field.get(targetObject));
            }
        }

        @Override
        public Boolean compareSate() throws IllegalAccessException {
            Field[] fieldsCur = targetObject.getClass().getDeclaredFields();
            for (Field field : fieldsCur) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                if (
                        (fieldsExclude.isEmpty() || !fieldsExclude.contains(field.getName())) &&
                                !(fieldsSave.containsKey(field.getName())
                                        && fieldsSave.get(field.getName()).equals(field.get(targetObject))
                                )
                )
                    return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "SnapShotField{" +
                    "fieldsSave=" + fieldsSave +
                    '}';
        }
    }


}


