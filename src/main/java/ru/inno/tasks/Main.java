package ru.inno.tasks;

public class Main {
    public static void main(String[] args) throws Exception {
        Fractionable numProxy = Utility.Cache(new Fraction(1, 4));
        numProxy.doubleValue();
        numProxy.setSum(6);
        numProxy.setNum(4);

        numProxy.doubleValue();
        numProxy.doubleValue(); // сработал
        numProxy.doubleValue(); // молчит
        numProxy.setNum(5);
        numProxy.doubleValue(); // сработал
        numProxy.doubleValue(); // молчит
        numProxy.setSum(100);
        System.out.println("Возвращаем состояние");
        numProxy.setNum(2);
        numProxy.doubleValue(); // молчит
        numProxy.doubleValue(); // молчит

        System.out.println("\n SLEEP 1500");
        Thread.currentThread().sleep(15);

        numProxy.doubleValue(); // сработал
        numProxy.doubleValue(); // молчит
        numProxy.setNum(5);
        numProxy.doubleValue(); // молчит

        numProxy.setNum(9);
        numProxy.doubleValue(); // сработал

        numProxy.sumValue(); // вычислено
        numProxy.sumValue(); // сработал

        numProxy.setNum(5);
        numProxy.doubleValue(); // молчит
    }
}

