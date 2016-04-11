package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_09
 * Author: DIYILIU
 * Update: 2016-04-11 14:25
 */

@Service
public class CMD_09 extends M2DataProcess {

    public CMD_09() {
        this.cmdId = 0x09;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.buffer(4);
        buf.writeShort((Integer) argus[0]);
        buf.writeShort((Integer) argus[1]);

        return headerToSendBytes(buf.array(), cmdId, m2Header);
    }
}
