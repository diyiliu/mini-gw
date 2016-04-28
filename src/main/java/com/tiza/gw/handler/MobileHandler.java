package com.tiza.gw.handler;

import com.tiza.model.BackupMSG;
import com.tiza.model.Tlv;
import com.tiza.model.header.MobileHeader;
import com.tiza.model.pipeline.MSGPipeline;
import com.tiza.model.pipeline.MSGTCPPipeline;
import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.util.CommonUtil;
import com.tiza.util.DateUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.dao.VehicleDao;
import com.tiza.util.entity.Instruction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Description: MobileHandler
 * Author: DIYILIU
 * Update: 2016-04-13 9:15
 */

@Component
@ChannelHandler.Sharable
public class MobileHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private ICache vehicleCacheProvider;

    @Resource
    private ICache monitorCacheProvider;

    @Resource
    protected ICache waitACKCacheProvider;

    @Resource
    private MobileDataProcess mobileDataProcess;

    @Resource
    private VehicleDao vehicleDao;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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

        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        MobileHeader mobileHeader = (MobileHeader) mobileDataProcess.dealHeader(bytes);
        String devIMEI = mobileHeader.getDevIMEI();

        if (!vehicleCacheProvider.containsKey(devIMEI)) {
            logger.warn("车辆未注册！[{}]", devIMEI);
            return;
        }

        // 重点监控
        if (monitorCacheProvider.containsKey(devIMEI)) {
            logger.info("收到消息，终端[{}], 命令[{}], 原始数据[{}]", devIMEI, CommonUtil.toHex(mobileHeader.getCmd()), CommonUtil.bytesToString(bytes));
        }
        Date now = new Date();
        if (!onlineCacheProvider.containsKey(devIMEI)) {
            logger.info("终端上线[{}], 上线时间[{}]", devIMEI, DateUtil.dateToString(now));
        }

        if (mobileHeader.getCmd() == 0x12) {

            mobileDataProcess.parse(null, mobileHeader);
            return;
        }
        // 响应
        response(mobileHeader, ctx);

        // 过滤重复数据
        if (onlineCacheProvider.containsKey(devIMEI)) {
            MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(devIMEI);
            Date receive = pipeline.getReceiveTime();
            MobileHeader header = (MobileHeader) pipeline.getHeader();

            if (header.getCmd() == mobileHeader.getCmd()
                    && (now.getTime() - receive.getTime()) / 1000.0 < 60) {

                logger.info("过滤[{}]重复数据[{}]", devIMEI, CommonUtil.toHex(mobileHeader.getCmd()));
                return;
            }
        }

        List<Tlv> tlvList = mobileHeader.getContent();
        for (Tlv tlv : tlvList) {
            MobileDataProcess process = (MobileDataProcess) mobileDataProcess.getMobileCMDCacheProvider().get(tlv.getTag());
            if (process == null) {
                logger.error("找不到[命令{}]解析器！", CommonUtil.toHex(tlv.getTag()));
                continue;
            }
            process.parse(tlv.getValue(), mobileHeader);
        }

        MSGTCPPipeline pipeline = new MSGTCPPipeline(ctx);
        pipeline.setReceiveTime(now);
        pipeline.setHeader(mobileHeader);
        onlineCacheProvider.put(devIMEI, pipeline);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务器异常...{}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    private void response(MobileHeader mobileHeader, ChannelHandlerContext ctx) {

        String terminalId = mobileHeader.getDevIMEI();
        // 查询待下发的指令信息
        List<Instruction> insList = vehicleDao.selectInstruction(mobileHeader.getDevIMEI(), 0);

        if (insList.isEmpty()) {

            // 中心无后续指令
            ack(mobileHeader, 0x00, ctx);
        } else {
            // 中心有命令下发
            ack(mobileHeader, 0x01, ctx);

            EventLoop eventLoop = ctx.channel().eventLoop();
            eventLoop.execute(() -> {

                byte[][] bytes = new byte[insList.size()][];

                for (int i = 0; i < insList.size(); i++) {
                    Instruction ins = insList.get(i);
                    int id = ins.getId();
                    int cmd = ins.getParamId();
                    String value = ins.getParamValue();

                    MobileDataProcess process = (MobileDataProcess) mobileDataProcess.getMobileCMDCacheProvider().get(cmd);
                    if (process == null) {
                        logger.error("找不到[命令{}]包装器！", CommonUtil.toHex(cmd));
                        continue;
                    }
                    byte[] content = process.pack(mobileHeader, value.split(","));
                    bytes[i] = content;

                    StringBuilder strb = new StringBuilder();
                    strb.append("UPDATE ").append(Constant.DBInfo.DB_CLOUD_USER).append(".").append(Constant.DBInfo.DB_CLOUD_INSTRUCTION)
                            .append(" SET ResponseStatus=1 WHERE ID=").append(id);

                    CommonUtil.dealToDb(strb.toString());

                    BackupMSG backupMSG = new BackupMSG(0, new Date(), terminalId, 0x12, toSendBytes(content, 0x12, terminalId),
                            Constant.Protocol.MOBILE_REPEAT_COUNT, Constant.Protocol.MOBILE_REPEAT_TIME);
                    backupMSG.setId(id);

                    waitACKCacheProvider.put(terminalId + cmd, backupMSG);
                }
                ByteBuf buf = Unpooled.copiedBuffer(bytes);
                byte[] array = toSendBytes(buf.array(), 0x12, terminalId);

                send(ctx, mobileHeader.getDevIMEI(), 0x12, array);
            });
        }
    }

    // 收到应答
    private void ack(MobileHeader mobileHeader, int result, ChannelHandlerContext ctx) {

        ByteBuf buf = Unpooled.buffer(7);
        buf.writeByte(result);
        buf.writeBytes(CommonUtil.dateToBytes(new Date()));

        byte[] content = toSendBytes(buf.array(), mobileHeader.getCmd(), mobileHeader.getDevIMEI());

        send(ctx, mobileHeader.getDevIMEI(), mobileHeader.getCmd(), content);
    }

    // 组装下发数据的协议头
    private byte[] toSendBytes(byte[] body, int cmdId, String terminalId) {

        int attr = body.length & 0x7FF;

        ByteBuf buf = Unpooled.buffer(11 + body.length);
        buf.writeBytes(CommonUtil.packIMEI(terminalId));
        buf.writeByte(cmdId);
        buf.writeShort(attr);
        buf.writeBytes(body);

        return buf.array();
    }

    // 数据下发
    private void send(ChannelHandlerContext ctx, String devIMEI, int cmd, byte[] content) {
        // 重点监控
        if (monitorCacheProvider.containsKey(devIMEI)) {
            logger.info("下发消息，终端[{}], 命令[{}], 原始数据[{}]", devIMEI, CommonUtil.toHex(cmd), CommonUtil.bytesToString(content));
        }

        if (ctx.channel().isWritable()) {
            ctx.writeAndFlush(Unpooled.copiedBuffer(content));
        } else {
            logger.error("数据下发失败，通道关闭！终端[{}], 命令[{}], 原始数据[{}]", devIMEI, CommonUtil.toHex(cmd), CommonUtil.bytesToString(content));
        }
    }

}
