package com.tiza.gw.server.tcp;

import com.tiza.gw.handler.Gl500Handler;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: Gl500Server
 * Author: DIYILIU
 * Update: 2016-03-15 15:21
 */

public class Gl500Server extends Thread implements IServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String END_MARK = "$";
    public static final int MAX_LENGTH = 1024;

    private int port = 8089;

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

                            ByteBuf delimiter = Unpooled.copiedBuffer(END_MARK.getBytes());

                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(MAX_LENGTH, delimiter))
                                    .addLast(new StringDecoder())
                                    .addLast(new Gl500Handler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            logger.info("Gl500服务器启动...");
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
