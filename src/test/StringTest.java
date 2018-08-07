import org.junit.Test;

public class StringTest {

    @Test
    public void test() {
        String str = "[startDate:2018-07-20 16:08:40][endDate:2018-07-20 16:08:40";
        System.out.println(str);
        System.out.println(str.substring(str.indexOf("[startDate:") + 11, str.indexOf("][")));
    }
}
