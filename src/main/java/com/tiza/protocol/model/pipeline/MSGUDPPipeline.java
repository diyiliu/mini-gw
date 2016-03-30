package com.tiza.protocol.model.pipeline;

import com.tiza.util.CommonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Description: MSGUDPPipeline
 * Author: DIYILIU
 * Update: 2016-03-18 16:38
 */

public class MSGUDPPipeline extends MSGPipeline {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ChannelHandlerContext ctx;

    private InetSocketAddress sender;

    public MSGUDPPipeline(ChannelHandlerContext ctx, InetSocketAddress sender) {
        this.ctx = ctx;
        this.sender = sender;
    }

    @Override
    public void send(String terminal, int cmd, byte[] bytes) {
        //logger.info("下发消息，终端[{}], 命令[{}H], 内容[{}]", terminal, CommonUtil.toHex(cmd), CommonUtil.bytesToString(bytes));

        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(bytes), sender));
    }
}
