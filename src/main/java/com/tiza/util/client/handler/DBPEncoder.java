package com.tiza.util.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Description: DBPEncoder
 * Author: DIYILIU
 * Update: 2016-03-23 10:58
 */
public class DBPEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        if (null != msg) {
            ByteBuf buf = (ByteBuf) msg;

            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            out.writeBytes(bytes);
        }
    }

}
