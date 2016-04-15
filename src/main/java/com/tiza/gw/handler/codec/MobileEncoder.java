package com.tiza.gw.handler.codec;

import com.tiza.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: MobileEncoder
 * Author: DIYILIU
 * Update: 2016-04-13 9:37
 */

public class MobileEncoder extends MessageToByteEncoder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        if (msg == null) {
            return;
        }

        ByteBuf byteBuf = (ByteBuf) msg;
        int length = byteBuf.readableBytes();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        byte[] imeiBytes = new byte[8];
        System.arraycopy(bytes, 0, imeiBytes, 0, 8);

        int cmd = bytes[8];

        byte check = CommonUtil.getCheck(bytes);

        byte[] array = new byte[length+ 1];
        array[length] = check;

        System.arraycopy(bytes, 0, array, 0, length);

        bytes = format(array);

        if (bytes == null) {
            ctx.close();
            return;
        }

        ByteBuf newBuf = Unpooled.buffer(bytes.length + 2);
        newBuf.writeByte(0x7E);
        newBuf.writeBytes(bytes);
        newBuf.writeByte(0x7E);

        byte[] rowData = newBuf.array();

        // 数据入库
        CommonUtil.toRawData(CommonUtil.parseIMEI(imeiBytes), cmd, 1, rowData);

        out.writeBytes(rowData);
    }

    /**
     * 0x7e ————> 0x7d 后紧跟一个0x02
     * 0x7d ————> 0x7d 后紧跟一个0x01
     *
     * @param bytes
     * @return
     */
    private byte[] format(byte[] bytes) {

        String hex = CommonUtil.bytesToStr(bytes).toUpperCase();

        hex.replaceAll("7D", "7D01");
        hex.replaceAll("7E", "7D02");

        byte[] array = CommonUtil.hexStringToBytes(hex);

        if (array == null) {
            logger.error("封装0x7D,0x7E异常！[{}]", hex);
        }

        return array;
    }

}
