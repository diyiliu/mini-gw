package com.tiza.gw.server.tcp;

import com.tiza.gw.handler.codec.MobileDecoder;
import com.tiza.gw.handler.codec.MobileEncoder;
import com.tiza.gw.server.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Description: MobileServer
 * Author: DIYILIU
 * Update: 2016-04-13 9:13
 */


public class MobileServer extends Thread implements IServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int port;

    @Resource
    private ChannelInboundHandler mobileHandler;

    @Override
    public void init() {

        this.start();
    }

    @Override
    public void run() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(new MobileEncoder())
                                    .addLast(new MobileDecoder())
                                    .addLast(mobileHandler);
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            logger.info("移动终端服务器启动...");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }
}
