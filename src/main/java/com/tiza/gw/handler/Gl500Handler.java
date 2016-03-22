package com.tiza.gw.handler;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: Gl500Handler
 * Author: DIYILIU
 * Update: 2016-03-15 16:03
 */

public class Gl500Handler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        String key = ctx.channel().remoteAddress().toString().trim().replaceFirst("/", "");

        logger.info("[{}]建立连接...", key);

        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isDone()) {
                    logger.info("[{}]关闭连接...", key);
                }
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("收到消息：" + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务器异常...{}", cause.getStackTrace());
        ctx.close();
    }

}
