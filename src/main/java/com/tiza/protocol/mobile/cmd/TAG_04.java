package com.tiza.protocol.mobile.cmd;

import com.tiza.model.header.Header;
import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_04
 * Author: DIYILIU
 * Update: 2016-04-15 11:07
 */

@Service
public class TAG_04 extends MobileDataProcess {

    public TAG_04() {
        this.cmdId = 0x04;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {

        String[] values = (String[]) argus;
        String ip = values[0];
        int port = Integer.parseInt(values[1]);

        ByteBuf buf = Unpooled.buffer(6);
        buf.writeBytes(CommonUtil.ipToBytes(ip));
        buf.writeShort(port);

        return toTLV(buf.array(), cmdId);
    }
}
