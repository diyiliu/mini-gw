import com.tiza.util.CommonUtil;
import com.tiza.util.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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


    @Test
    public void testArray(){

        Object[] objects = new Object[]{"abc"};

        List list = Arrays.asList(objects);

        String[] values = new String[list.size()];
        list.toArray(values);

        System.out.println(values[0]);
    }

    @Test
    public void testByte(){

        /**
        ByteBuf byteBuf = Unpooled.buffer(2);
        byteBuf.writeByte(0x8e);
        byteBuf.writeByte(0x8e);

        System.out.println(0x8e);
        System.out.println(byteBuf.readByte());
        System.out.println(byteBuf.readUnsignedByte());
         */

        short i = 0x3ff;

        System.out.println(i & 0x4ff);
    }

    @Test
    public void testByteString(){
        byte[] array = new byte[]{0x03, 0x3D, 0x55, 0x7A, 0x39};

        String str = CommonUtil.bytesToStr(array);

        array = CommonUtil.hexStringToBytes(str);

        System.out.println(CommonUtil.bytesToString(array));
    }
}
