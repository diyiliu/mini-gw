package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_8D
 * Author: DIYILIU
 * Update: 2016-03-29 10:05
 */

@Service
public class CMD_8D extends M2DataProcess {

    public CMD_8D() {
        this.cmdId = 0x8D;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] bytes = new byte[22];
        buf.readBytes(bytes);

        byte power = buf.readByte();

        Position position = renderPosition(bytes);
        Status status = renderStatu(position.getStatus());

        toDB(m2Header.getTerminalId(), position, status);

        send(0x02, m2Header);
    }
}
