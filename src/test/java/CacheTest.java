import ru.inno.tasks.*;

import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CacheTest {

    @Test
    @Description("Call using Fraction class")
    void doubleFraction(){
        FractionTest fractionTest = new FractionTest(2, 4);
        Fractionable fraction = Utility.Cache(fractionTest);

        fraction.doubleValue();
        Assertions.assertEquals(1, fractionTest.cmpCnt);
        fraction.doubleValue();
        Assertions.assertEquals(1, fractionTest.cmpCnt);

        fraction.setNum(5);
        Assertions.assertEquals(1, fractionTest.cmpCntMutator);
        Assertions.assertEquals(1, fractionTest.cmpCnt);

        fraction.doubleValue();
        Assertions.assertEquals(2, fractionTest.cmpCnt);
        fraction.doubleValue();
        Assertions.assertEquals(2, fractionTest.cmpCnt);

        fraction.setNum(2);
        Assertions.assertEquals(2, fractionTest.cmpCntMutator);
        fraction.doubleValue();
        Assertions.assertEquals(2, fractionTest.cmpCnt);
        fraction.doubleValue();
        Assertions.assertEquals(2, fractionTest.cmpCnt);

        CountDownLatch waiter = new CountDownLatch(1);
        try {
            waiter.await(1500* 1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        fraction.doubleValue();
        Assertions.assertEquals(3, fractionTest.cmpCnt);
        fraction.doubleValue();
        Assertions.assertEquals(3, fractionTest.cmpCnt);

        fraction.setSum(5);
        fraction.doubleValue();
        Assertions.assertEquals(3, fractionTest.cmpCnt);
        fraction.sumValue();
        Assertions.assertEquals(3, fractionTest.cmpCnt);

        fraction.setNum(9);
        fraction.doubleValue();
        Assertions.assertEquals(4, fractionTest.cmpCnt);
        Assertions.assertEquals(3, fractionTest.cmpCntMutator);

        fraction.multiplyValue();
        Assertions.assertEquals(1, fractionTest.cmpCntMulti);
        fraction.multiplyValue();
        Assertions.assertEquals(1, fractionTest.cmpCntMulti);
        Assertions.assertEquals(4, fractionTest.cmpCnt);
    }

    @Test
    @Description("Methods without annotation")
    void doubleValueFractionTest(){

        FractionTest fractionTest = new FractionTest(20, 4);
        Fractionable fr = Utility.Cache(fractionTest);
        for(int i = 0; i < 4; i++)
            fr.toString();
        Assertions.assertEquals(4, fractionTest.cmpCnt);
    }
    @Test
    @Description("Method with the Mutator annotation")
    void doubleValueFractionTestMutator(){
        FractionTest fractionTest = new FractionTest(20, 4);
        Fractionable fr = Utility.Cache(fractionTest);
        for(int i = 0; i < 4; i++)
            fr.setNum(40);
        Assertions.assertEquals(0, fractionTest.cmpCnt);
        Assertions.assertEquals(4, fractionTest.cmpCntMutator);
    }

    @Test
    @Description("Method with the Cache annotation")
    void doubleValueFractionTestCache(){
        FractionTest fractionTest = new FractionTest(20, 4);
        Fractionable fr = Utility.Cache(fractionTest);
        for(int i = 0; i < 4; i++)
            fr.doubleValue();
        Assertions.assertEquals(1, fractionTest.cmpCnt);
    }
    @Test
    @Description("Tests")
    void doubleValueFractionTests(){

        FractionTest fractionTest = new FractionTest(20, 4);
        Fractionable fr = Utility.Cache(fractionTest);
        fr.doubleValue();
        fr.doubleValue();
        Assertions.assertEquals(1, fractionTest.cmpCnt);
        fr.toString();
        fr.toString();
        Assertions.assertEquals(3, fractionTest.cmpCnt);
        fr.doubleValue();
        Assertions.assertEquals(3, fractionTest.cmpCnt);
        fr.setNum(40);
        Assertions.assertEquals(3, fractionTest.cmpCnt);
        Assertions.assertEquals(1, fractionTest.cmpCntMutator);

        fr.doubleValue();
        Assertions.assertEquals(4, fractionTest.cmpCnt);
        fr.doubleValue();
        fr.doubleValue();
        Assertions.assertEquals(4, fractionTest.cmpCnt);

    }

    @Test
    @Description("Testing cache values")
    void DoubleValue() {
        Fractionable numProxy = Utility.Cache(new Fraction(10,2));
        Assertions.assertEquals(5, numProxy.doubleValue());
        numProxy.doubleValue(); // молчит
        numProxy.doubleValue(); // молчит
        numProxy.setNum(20);
        Assertions.assertEquals(10, numProxy.doubleValue());
        numProxy.doubleValue(); // сработал
        Assertions.assertEquals(10, numProxy.doubleValue());
        numProxy.doubleValue(); // молчит
        Assertions.assertEquals(10, numProxy.doubleValue());
        numProxy.setNumForTest(2);
        Assertions.assertNotEquals(1, numProxy.doubleValue());
        numProxy.setNum(2);
        numProxy.setDenum(4);
        Assertions.assertEquals(8, numProxy.multiplyValue());
        numProxy.doubleValue(); // молчит
        Assertions.assertEquals(8, numProxy.multiplyValue());
        numProxy.doubleValue(); // молчит
        Assertions.assertEquals(8, numProxy.multiplyValue());
        numProxy.setNum(4);
        Assertions.assertEquals(16, numProxy.multiplyValue());
        numProxy.doubleValue(); // сработал
        Assertions.assertEquals(16, numProxy.multiplyValue());
        numProxy.doubleValue(); // молчит
        Assertions.assertEquals(16, numProxy.multiplyValue());
        numProxy.setNumForTest(5);
        Assertions.assertEquals(20.0, numProxy.multiplyValue());

    }


}

