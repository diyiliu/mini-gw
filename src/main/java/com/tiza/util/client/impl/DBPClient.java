package com.tiza.util.client.impl;

import com.tiza.util.client.IClient;
import com.tiza.util.client.handler.DBPDecoder;
import com.tiza.util.client.handler.DBPEncoder;
import com.tiza.util.client.handler.DBPHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description: DBPClient
 * Author: DIYILIU
 * Update: 2016-03-23 10:31
 */
public class DBPClient extends Thread implements IClient {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ConcurrentLinkedQueue<String> sqlPool = new ConcurrentLinkedQueue<>();

    private ExecutorService executor = Executors.newScheduledThreadPool(1);

    private final static int RETRY_COUNT = 5;
    private int count = 0;

    private String host = "192.168.1.19";

    private int port = 8088;

    @Resource
    private DBPHandler dbpHandler;

    @Override
    public void init() {
        this.start();

        executor.execute(() -> {
            for (; ; ) {
                while (!sqlPool.isEmpty()) {
                    if (dbpHandler.isActive()) {
                        String sql = sqlPool.poll();
                        dbpHandler.send(sql);
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void run() {
        connectDBP(host, port);
    }

    public void connectDBP(String host, int port) {

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {

                        ch.pipeline().addLast(new DBPEncoder())
                                .addLast(new DBPDecoder())
                                .addLast(new IdleStateHandler(0, 40, 0))
                                .addLast(dbpHandler);
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            logger.info("DBP客户端启动...");
            count = 0;
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            dbpHandler.setActive(Boolean.FALSE);

            if (count > RETRY_COUNT - 1) {
                logger.warn("DBP客户端重连失败！");
                return;
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("DBP客户端, 尝试第{}次重连...", ++count);
            connectDBP(host, port);
        }
    }

    public static void sendSQL(String sql) {
        sqlPool.add(sql);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
