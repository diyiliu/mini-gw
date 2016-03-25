import com.tiza.util.DateUtil;
import org.junit.Test;

import java.util.Date;

/**
 * Description: MainTest
 * Author: DIYILIU
 * Update: 2016-03-25 11:32
 */
public class MainTest {

    @Test
    public void test() {

        System.out.println(DateUtil.dateToString(new Date(), "%1$tY%1$tm"));
    }
}
