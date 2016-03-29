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
 * Description: CMD_8B
 * Author: DIYILIU
 * Update: 2016-03-29 9:43
 */

@Service
public class CMD_8B extends M2DataProcess {

    public CMD_8B() {
        this.cmdId = 0x8B;
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

        int size = buf.readUnsignedByte();

        for (int i = 0; i < size; i++) {

            int cmd = buf.readUnsignedByte();
            int length = buf.readUnsignedShort();

            byte[] bytes = new byte[length];
            buf.readBytes(bytes);

            if (m2CMDCacheProvider.containsKey(cmd)) {
                M2DataProcess process = (M2DataProcess) m2CMDCacheProvider.get(cmd);
                process.parse(bytes, m2Header);
            } else {
                logger.error("盲区补偿：找不到[命令{}]解析器！", CommonUtil.toHex(cmd));
            }
        }

        send(0x02, m2Header);
    }
}
