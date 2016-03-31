package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_04
 * Author: DIYILIU
 * Update: 2016-03-30 11:29
 */

@Service
public class CMD_04 extends M2DataProcess {

    public CMD_04() {
        this.cmdId = 0x04;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        int paramId = (int) argus[0];
        Object[] values = (Object[]) argus[1];
        
        byte[] value;
        switch (paramId) {
            case 0x06:
                value = CommonUtil.ipToBytes((String) values[0]);
                break;
            case 0x08:
                value = CommonUtil.ipToBytes((String) values[0]);
                break;
            case 0x0A:
                value = CommonUtil.longToBytes((int) values[0], 2);
                break;
            case 0x0B:
                value = new byte[]{(byte) ((int) values[0])};
                break;
            case 0x0D:
                value = CommonUtil.longToBytes((int) values[0], 2);
                break;
            case 0x0E:
                ByteBuf b = Unpooled.buffer(4);
                b.writeBytes(CommonUtil.longToBytes((int) values[0], 2));
                b.writeBytes(CommonUtil.longToBytes((int) values[1], 2));
                value = b.array();
                break;
            case 0x0F:
                value = CommonUtil.longToBytes((int) values[0], 5);
                break;
            case 0x10:
                value = CommonUtil.longToBytes((int) values[0], 4);
                break;
            default:
                String paramValue = (String) values[0];
                value = paramValue.getBytes();
        }

        ByteBuf buf = Unpooled.buffer(2 + 1 + value.length);
        buf.writeShort(paramId);
        buf.writeByte(value.length);
        buf.writeBytes(value);

        return headerToSendBytes(buf.array(), cmdId, m2Header);
    }
}
