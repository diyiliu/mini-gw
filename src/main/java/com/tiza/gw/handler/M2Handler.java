package com.tiza.gw.handler;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.protocol.model.pipeline.MSGUDPPipeline;
import com.tiza.util.CommonUtil;
import com.tiza.util.JacksonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.entity.VehicleInfo;
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
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        DatagramPacket packet = (DatagramPacket) msg;

        ByteBuf buf = packet.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        if (bytes.length < 17) {
            logger.error("数据长度不足17位[{}]", CommonUtil.bytesToString(bytes));
            return;
        }

        M2Header m2Header = m2DataProcess.dealHeader(bytes);
        if (m2Header == null) {
            return;
        }

        logger.info("收到消息，终端[{}], 命令[{}], 原始数据[{}]", m2Header.getTerminalId(), CommonUtil.toHex(m2Header.getCmd()), CommonUtil.bytesToString(bytes));
        toDB(m2Header, bytes);

        MSGPipeline pipeline = new MSGUDPPipeline(ctx, packet.sender());
        pipeline.setReceiveTime(new Date());
        pipeline.setHeader(m2Header);
        onlineCacheProvider.put(m2Header.getTerminalId(), pipeline);

        M2DataProcess process = (M2DataProcess) m2DataProcess.getM2CMDCacheProvider().get(m2Header.getCmd());
        if (process == null) {
            logger.error("找不到[命令{}]解析器！", CommonUtil.toHex(m2Header.getCmd()));
            return;
        }

        if (vehicleCacheProvider.containsKey(m2Header.getTerminalId())){
            process.parse(m2Header.getContent(), m2Header);
        }else {
            logger.warn("车辆未注册！[{}]", m2Header.getTerminalId());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("服务器异常: {}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    public void toDB(M2Header m2Header, byte[] content) {

        Map map = new HashMap() {{
            this.put("DeviceId", m2Header.getTerminalId());
            this.put("ReceiveTime", new Date());
            this.put("DataFlow", 0);
            this.put("Instruction", CommonUtil.toHex(m2Header.getCmd()));
            this.put("RawData", CommonUtil.bytesToStr(content));
        }};

        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER, CommonUtil.monthTable(Constant.DBInfo.DB_CLOUD_RAWDATA, new Date()), map);
    }
}
