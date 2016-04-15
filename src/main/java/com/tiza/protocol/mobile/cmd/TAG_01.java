package com.tiza.protocol.mobile.cmd;

import com.tiza.model.header.Header;
import com.tiza.model.header.MobileHeader;
import com.tiza.protocol.mobile.MobileDataProcess;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_01
 * Author: DIYILIU
 * Update: 2016-04-15 11:07
 */

@Service
public class TAG_01 extends MobileDataProcess {

    public TAG_01() {
        this.cmdId = 0x01;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        String[] values = (String[]) argus;
        int mm = Integer.parseInt(values[0]);
        int day = Integer.parseInt(values[1].substring(0, 2), 16);
        int hour = Integer.parseInt(values[1].substring(2, 4), 16);
        int minute = Integer.parseInt(values[1].substring(4), 16);
        int worktime = Integer.parseInt(values[2]);

        ByteBuf buf = Unpooled.buffer(6);
        buf.writeByte(mm & 0x03);
        buf.writeByte(day);
        buf.writeByte(hour);
        buf.writeByte(minute);
        buf.writeShort(worktime);

        return toTLV(buf.array(), cmdId);
    }
}
