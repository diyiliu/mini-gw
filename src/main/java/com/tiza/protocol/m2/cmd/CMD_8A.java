package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_8A
 * Author: DIYILIU
 * Update: 2016-03-29 9:38
 */

@Service
public class CMD_8A extends M2DataProcess {

    public CMD_8A() {
        this.cmdId = 0x8A;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);
        byte alarm = buf.readByte();

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        Position position = renderPosition(bytes);
        Status status = renderStatu(position.getStatus());

        toDB(m2Header.getTerminalId(), position, status);

        send(0x02, m2Header);
    }
}
