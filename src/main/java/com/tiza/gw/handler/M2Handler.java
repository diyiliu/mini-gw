package com.tiza.gw.handler;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.M2Header;
import com.tiza.model.pipeline.MSGPipeline;
import com.tiza.model.pipeline.MSGUDPPipeline;
import com.tiza.util.CommonUtil;
import com.tiza.util.DateUtil;
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
@ChannelHandler.Sharable
public class M2Handler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private ICache vehicleCacheProvider;

    @Resource
    private M2DataProcess m2DataProcess;

    @Resource
    private ICache monitorCacheProvider;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket packet = (DatagramPacket) msg;

        ByteBuf buf = packet.content();

        buf.markReaderIndex();
        int length = buf.readUnsignedShort();
        buf.resetReaderIndex();

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        if (bytes.length < length + 3) {
            logger.error("数据包不完整！[{}]", CommonUtil.bytesToString(bytes));
            return;
        }

        M2Header m2Header = m2DataProcess.dealHeader(bytes);

        // 数据入库
        CommonUtil.toRawData(m2Header.getTerminalId(), m2Header.getCmd(), 0, bytes);

        if (!vehicleCacheProvider.containsKey(m2Header.getTerminalId())) {
            logger.warn("车辆未注册！[{}]", m2Header.getTerminalId());
            return;
        }

        M2DataProcess process = (M2DataProcess) m2DataProcess.getM2CMDCacheProvider().get(m2Header.getCmd());
        if (process == null) {
            logger.error("找不到[命令{}]解析器！", CommonUtil.toHex(m2Header.getCmd()));
            return;
        }

        // 重点监控
        if (monitorCacheProvider.containsKey(m2Header.getTerminalId())) {
            logger.info("收到消息，终端[{}], 命令[{}], 原始数据[{}]", m2Header.getTerminalId(), CommonUtil.toHex(m2Header.getCmd()), CommonUtil.bytesToString(bytes));
        }
        Date now = new Date();
        // 数据解析
        process.parse(m2Header.getContent(), m2Header);

        if (!onlineCacheProvider.containsKey(m2Header.getTerminalId())) {
            logger.info("终端上线[{}], 上线时间[{}]", m2Header.getTerminalId(), DateUtil.dateToString(now));
        }

        MSGPipeline pipeline = new MSGUDPPipeline(ctx, packet.sender());
        pipeline.setReceiveTime(now);
        pipeline.setHeader(m2Header);
        onlineCacheProvider.put(m2Header.getTerminalId(), pipeline);
        /**
         // 登录指令
         if (m2Header.getCmd() == 0x85) {
         onlineCacheProvider.put(m2Header.getTerminalId(), pipeline);
         }*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("服务器异常: {}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
