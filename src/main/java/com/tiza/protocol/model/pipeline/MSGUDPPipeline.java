package com.tiza.protocol.model.pipeline;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * Description: MSGUDPPipeline
 * Author: DIYILIU
 * Update: 2016-03-18 16:38
 */

public class MSGUDPPipeline extends MSGPipeline{

    private ChannelHandlerContext ctx;

    private InetSocketAddress sender;

    public MSGUDPPipeline(ChannelHandlerContext ctx, InetSocketAddress sender) {
        this.ctx = ctx;
        this.sender = sender;
    }

    @Override
    public void send(String terminal, byte[] bytes) {

        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes), sender));
    }
}
