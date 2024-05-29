
import org.junit.jupiter.api.*;
import ru.inno.tasks.Fractionable;
import ru.inno.tasks.Utils;

public class CacheHandlerTests {

    TestFraction testFraction = new TestFraction(1, 4);
    Fractionable fraction2 = Utils.cache(testFraction);

    @Test
    @DisplayName("Methods without annotation")
    public void testNoAnnotation() {
        fraction2.toString();
        fraction2.toString();
        fraction2.toString();
        Assertions.assertEquals(testFraction.count, 3);
    }

    @Test
    @DisplayName("Methods with annotation without clearing cache")
    public void testCacheNoClear() {
        fraction2.doubleValue();
        fraction2.doubleValue();
        fraction2.toString();
        fraction2.doubleValue();
        Assertions.assertEquals(testFraction.count, 2);
    }
    @Test
    @DisplayName("Methods with annotation and cache clearing")
    public void testCacheClear() {
        fraction2.doubleValue();
        fraction2.setDenum(5);
        fraction2.toString();
        fraction2.doubleValue();
        fraction2.setNum(2);
        fraction2.doubleValue();
        fraction2.doubleValue();
        Assertions.assertEquals(testFraction.count, 4);
    }


}
