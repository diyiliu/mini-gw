package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_02
 * Author: DIYILIU
 * Update: 2016-03-21 15:20
 */

@Service
public class CMD_02 extends M2DataProcess{

    public CMD_02() {
        this.cmdId = 0x02;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.buffer(4);
        buf.writeShort(m2Header.getSerial());
        buf.writeByte(m2Header.getCmd());
        buf.writeByte(0);

        return headerToSendBytes(buf.array(), this.cmdId, m2Header);
    }
}
