package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_8C
 * Author: DIYILIU
 * Update: 2016-03-29 10:02
 */

@Service
public class CMD_8C extends M2DataProcess {

    public CMD_8C() {
        this.cmdId = 0x8C;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte[] bytes = new byte[22];
        buf.readBytes(bytes);

        // TODO: 2016/3/29

        Position position = renderPosition(bytes);
        Status status = renderStatu(position.getStatus());

        toDB(m2Header.getTerminalId(), position, status);

        send(0x02, m2Header);
    }
}
