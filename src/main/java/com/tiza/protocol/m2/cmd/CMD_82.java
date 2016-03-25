package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Description: CMD_82
 * Author: DIYILIU
 * Update: 2016-03-21 14:51
 */

@Service
public class CMD_82 extends M2DataProcess {

    public CMD_82() {
        this.cmdId = 0x82;
    }

    @Resource
    private ICache waitACKCacheProvider;

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);
        int serial = buf.readUnsignedShort();
        int cmd = buf.readByte();
        int result = buf.readByte();
        //logger.info("解析命令[{}]，序号[{}], 下行命令[{}], 结果[{}], 车辆[{}]", CommonUtil.toHex(m2Header.getCmd()), serial, CommonUtil.toHex(cmd), result, m2Header.getTerminalId());

        if (waitACKCacheProvider.containsKey(serial)){
            logger.info("收到应答，序号[{}], 命令[{}], 结果[{}], 车辆[{}]", serial, CommonUtil.toHex(cmd), result, m2Header.getTerminalId());
            waitACKCacheProvider.remove(serial);
        }
    }
}
