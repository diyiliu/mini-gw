package com.tiza.gw.handler.codec;

import com.tiza.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Description: MobileDecoder
 * Author: DIYILIU
 * Update: 2016-04-13 9:34
 */
public class MobileDecoder extends ByteToMessageDecoder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 2) {
            return;
        }

        in.markReaderIndex();
        // 处理上条异常数据(0x7E,0x7E)
        byte[] headerFooter = new byte[2];
        in.readBytes(headerFooter);
        if (headerFooter[0] == headerFooter[1]) {
            in.resetReaderIndex();
            in.readByte();
            in.markReaderIndex();
        }
        in.resetReaderIndex();

        in.markReaderIndex();
        int header = in.readUnsignedByte();
        if (header != 0x7E) {
            logger.error("消息头校验失败！[{}]", CommonUtil.toHex(header));
            return;
        }

        if (in.readableBytes() < 14) {

            in.resetReaderIndex();
            return;
        }

        byte[] imeiBytes = new byte[8];
        in.readBytes(imeiBytes);

        int cmd = in.readUnsignedByte();

        short atrr = in.readShort();

        // 消息体长度
        int length = atrr & 0x3FF;

        if (in.readableBytes() < length + 2) {
            in.resetReaderIndex();
            return;
        }
        in.resetReaderIndex();

        in.markReaderIndex();

        byte[] rowData = new byte[length + 14];
        in.readBytes(rowData);
        // 数据入库
        CommonUtil.toRawData(CommonUtil.parseIMEI(imeiBytes), cmd, 0, rowData);

        in.resetReaderIndex();

        in.readByte();

        byte[] bytes = new byte[length + 11];
        in.readBytes(bytes);

        byte check = CommonUtil.getCheck(bytes);
        byte realCK = in.readByte();

        if (check != realCK) {
            logger.error("校验和校验失败！[{},{}], 原始数据[{}]", CommonUtil.toHex(check), CommonUtil.toHex(realCK), CommonUtil.bytesToString(rowData));
            // 应答数据有误！
            ackError(ctx, imeiBytes, cmd);
            // 读到本条结束位
            while (in.readableBytes() > 0) {
                if (in.readUnsignedByte() == 0x7E) {
                    break;
                }
            }

            return;
        }

        int footer = in.readUnsignedByte();
        if (footer != 0x7E) {
            logger.error("结束位校验失败！[{}]", CommonUtil.toHex(header));
            return;
        }

        bytes = format(bytes);
        if (bytes == null) {
            return;
        }

        out.add(Unpooled.copiedBuffer(bytes));
    }

    /**
     * 0x7d0x02 ————> 0x7e
     * 0x7d0x01 ————> 0x7d
     *
     * @param bytes
     * @return
     */
    private byte[] format(byte[] bytes) {

        String hex = CommonUtil.bytesToStr(bytes).toUpperCase();

        hex.replaceAll("7D01", "7D");
        hex.replaceAll("7D02", "7E");

        byte[] array = CommonUtil.hexStringToBytes(hex);

        if (array == null) {
            logger.error("解封装0x7D01,0x7D02异常！[{}]", hex);
        }

        return array;
    }

    private void ackError(ChannelHandlerContext ctx, byte[] imeiBytes, int cmd) {

        int attr = 7 & 0x7FF;

        ByteBuf buf = Unpooled.buffer(11 + 7);
        buf.writeBytes(imeiBytes);
        buf.writeByte(cmd);
        buf.writeShort(attr);
        buf.writeByte(0x10);
        buf.writeBytes(CommonUtil.dateToBytes(new Date()));

        ctx.writeAndFlush(buf);
    }
}
