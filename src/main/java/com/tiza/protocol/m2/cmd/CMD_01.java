package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_01
 * Author: DIYILIU
 * Update: 2016-03-21 11:13
 */

@Service
public class CMD_01 extends M2DataProcess {

    public CMD_01() {
        this.cmdId = 0x01;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        String apn = "CMNET";
        String ip = "192.168.1.19";
        int port = 6600;

        byte[] apnBytes = apn.getBytes();

        ByteBuf buf = Unpooled.buffer(1 + apnBytes.length + 4 + 2);
        buf.writeByte(apnBytes.length);
        buf.writeBytes(apnBytes);
        buf.writeBytes(CommonUtil.ipToBytes(ip));
        buf.writeShort(port);

        return headerToSendBytes(buf.array(), this.cmdId, m2Header);
    }
}
