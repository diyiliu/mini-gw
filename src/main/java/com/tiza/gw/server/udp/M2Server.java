package com.tiza.gw.server.udp;

import com.tiza.gw.server.IServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Description: M2Server
 * Author: DIYILIU
 * Update: 2016-03-15 15:21
 */
public class M2Server extends Thread implements IServer{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int port = 8889;

    @Resource
    private ChannelInboundHandler m2Handler;

    public void init() {
        this.start();
    }

    @Override
    public void run() {
        logger.info("M2服务器启动...");

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();

            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(m2Handler);

            // 绑定端口，同步等待成功
            b.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }


}
