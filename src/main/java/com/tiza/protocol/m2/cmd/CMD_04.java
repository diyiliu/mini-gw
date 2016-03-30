package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
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
        String paramValue = (String) argus[1];
        byte[] bytes = paramValue.getBytes();

        ByteBuf buf = Unpooled.buffer(2 + 1 + bytes.length);
        buf.writeShort(paramId);
        buf.writeByte(bytes.length);
        buf.writeBytes(bytes);

        return headerToSendBytes(buf.array(), cmdId, m2Header);
    }
}
