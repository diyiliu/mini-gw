package com.tiza.gw.handler;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.protocol.model.pipeline.MSGUDPPipeline;
import com.tiza.util.Common;
import com.tiza.util.cache.ICache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Description: M2Handler
 * Author: DIYILIU
 * Update: 2016-03-17 14:04
 */

@Component
public class M2Handler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private M2DataProcess m2DataProcess;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        DatagramPacket packet = (DatagramPacket) msg;

        ByteBuf buf = packet.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        if (bytes.length < 14) {
            logger.error("数据长度不足14位[{}]", Common.byteToString(bytes));
        }

        M2Header m2Header = m2DataProcess.dealHeader(bytes);
        logger.info("收到消息，终端[{}], 命令[{}], 原始数据[{}]", m2Header.getTerminalId(), Common.toHex(m2Header.getCmd()), Common.byteToString(bytes));

        MSGPipeline pipeline = new MSGUDPPipeline(ctx, packet.sender());
        pipeline.setReceiveTime(new Date());
        pipeline.setHeader(m2Header);
        onlineCacheProvider.put(m2Header.getTerminalId(), pipeline);

        M2DataProcess process = (M2DataProcess) m2DataProcess.getM2CMDCacheProvider().get(m2Header.getCmd());
        if (process == null) {
            logger.error("找不到[命令{}]解析器！", Common.toHex(m2Header.getCmd()));
            return;
        }
        process.parse(m2Header.getContent(), m2Header);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("服务器异常: {}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
