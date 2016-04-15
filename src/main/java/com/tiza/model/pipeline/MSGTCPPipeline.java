package com.tiza.model.pipeline;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * Description: MSGTCPPipeline
 * Author: DIYILIU
 * Update: 2016-04-13 16:29
 */
public class MSGTCPPipeline extends MSGPipeline{

    private ChannelHandlerContext ctx;

    public MSGTCPPipeline(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void send(String terminal, int cmd, byte[] bytes) {
        setSendTime(new Date());
        ctx.writeAndFlush(Unpooled.copiedBuffer(bytes));
    }
}
