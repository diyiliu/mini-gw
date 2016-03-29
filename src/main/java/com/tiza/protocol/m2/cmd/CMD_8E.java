package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Description: CMD_8E
 * Author: DIYILIU
 * Update: 2016-03-29 10:08
 */

@Service
public class CMD_8E extends M2DataProcess {

    public CMD_8E() {
        this.cmdId = 0x8E;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        long lat = buf.readUnsignedInt();
        long lng = buf.readUnsignedInt();

        byte[] timeBytes = new byte[6];
        buf.readBytes(timeBytes);
        Date dateTime = CommonUtil.bytesToDate(timeBytes);

        // TODO: 2016/3/29
    }
}
