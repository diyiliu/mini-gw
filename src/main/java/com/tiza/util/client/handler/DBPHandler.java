package com.tiza.util.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * Description: DBPHandler
 * Author: DIYILIU
 * Update: 2016-03-23 10:56
 */

@Component
@ChannelHandler.Sharable
public class DBPHandler extends ChannelInboundHandlerAdapter{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean active;

    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        this.active = Boolean.TRUE;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("通讯异常：{}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // 心跳处理
        if (evt instanceof IdleStateEvent) {

            IdleStateEvent event = (IdleStateEvent) evt;

            if (IdleState.READER_IDLE == event.state()) {
                //logger.info("读超时...");

            } else if (IdleState.WRITER_IDLE == event.state()) {
                //logger.warn("写超时...");
                ByteBuf buf = Unpooled.buffer(3);
                buf.writeShort(3);
                buf.writeByte(0x00);
                //发送心跳
                //logger.info("发送心跳...");
                ctx.writeAndFlush(buf);
            } else if (IdleState.ALL_IDLE == event.state()) {
                //logger.warn("读/写超时...");
            }
        }
    }

    public void send(String sql){
        byte[] content = sql.getBytes(Charset.forName("UTF-8"));
        int length = content.length + 3;
        ByteBuf buf = Unpooled.buffer(length);
        buf.writeShort(length);
        buf.writeByte(0x01);
        buf.writeBytes(content);

        //logger.info("发送DBP: [{}]", sql);
        ctx.writeAndFlush(buf);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
