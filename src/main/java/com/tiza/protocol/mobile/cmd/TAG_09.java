package com.tiza.protocol.mobile.cmd;

import com.tiza.model.header.Header;
import com.tiza.protocol.mobile.MobileDataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_09
 * Author: DIYILIU
 * Update: 2016-04-15 11:07
 */

@Service
public class TAG_09  extends MobileDataProcess{

    public TAG_09() {
        this.cmdId = 0x09;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {

        String[] values = (String[]) argus;
        int time = Integer.parseInt(values[0]);
        int period = Integer.parseInt(values[1]);

        ByteBuf buf = Unpooled.buffer(4);
        buf.writeShort(time);
        buf.writeShort(period);

        return toTLV(buf.array(), cmdId);
    }
}
