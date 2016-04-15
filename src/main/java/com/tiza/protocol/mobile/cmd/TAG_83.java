package com.tiza.protocol.mobile.cmd;

import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.model.header.Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_83
 * Author: DIYILIU
 * Update: 2016-04-14 10:21
 */

@Service
public class TAG_83 extends MobileDataProcess {

    public TAG_83() {
        this.cmdId = 0x83;
    }

    @Override
    public void parse(byte[] content, Header header) {

        ByteBuf buf = Unpooled.copiedBuffer(content);

        int mcc = buf.readUnsignedShort();
        int mnc = buf.readUnsignedByte();
        int cellNum = buf.readUnsignedByte();

        for (int i = 0; i < cellNum; i++) {
            if (buf.readableBytes() < 5) {
                break;
            }
            int lac = buf.readUnsignedShort();
            int cellId = buf.readUnsignedShort();
            int signal = buf.readUnsignedByte();

            //logger.info("小区编号:{}, 基站编号:{}, 信号强度:{}", lac, cellId, signal);
        }
        // TODO: 2016/4/14  
    }
}
