package com.tiza.protocol.m2;

import com.tiza.protocol.IDataProcess;
import com.tiza.protocol.model.RepeatMSG;
import com.tiza.protocol.model.SendMSG;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.Common;
import com.tiza.util.cache.ICache;
import com.tiza.util.task.impl.MSGSenderTask;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: M2DataProcess
 * Author: DIYILIU
 * Update: 2016-03-21 10:05
 */

@Service
public class M2DataProcess implements IDataProcess {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    protected ICache m2CMDCacheProvider;

    @Resource
    protected ICache waitACKCacheProvider;

    protected int cmdId = 0xFF;

    @Override
    public M2Header dealHeader(byte[] bytes) {

        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        int length = buf.readUnsignedShort();

        byte[] termi = new byte[5];
        buf.readBytes(termi);
        String terminalId = Common.parseSIM(termi);

        int version = buf.readByte();

        int factory = buf.readByte();

        int terminalType = buf.readByte();

        int user = buf.readByte();

        int serial = buf.readUnsignedShort();

        int cmd = buf.readUnsignedByte();

        byte[] content = new byte[buf.readableBytes() - 3];
        buf.readBytes(content);

        int check = buf.readByte();

        byte[] end = new byte[2];
        buf.readBytes(end);

        return new M2Header(cmd, length, terminalId,
                version, factory, terminalType,
                user, serial, content,
                check, end);
    }

    /**
     * 消息头（不包含校验位、结束位）
     *
     * @param header
     * @return
     */
    public byte[] headerToContent(M2Header header) {

        ByteBuf buf = Unpooled.buffer(header.getLength());
        buf.writeShort(header.getLength());
        buf.writeBytes(Common.packSIM(header.getTerminalId()));
        buf.writeByte(header.getVersion());
        buf.writeByte(header.getFactory());
        buf.writeByte(header.getTerminalType());
        buf.writeByte(header.getUser());
        buf.writeShort(header.getSerial());
        buf.writeByte(header.getCmd());
        buf.writeBytes(header.getContent());

        return buf.array();
    }

    public byte[] headerToBytes(M2Header header) {

        ByteBuf buf = Unpooled.buffer(header.getLength() + 3);
        buf.writeShort(header.getLength());
        buf.writeBytes(Common.packSIM(header.getTerminalId()));
        buf.writeByte(header.getVersion());
        buf.writeByte(header.getFactory());
        buf.writeByte(header.getTerminalType());
        buf.writeByte(header.getUser());
        buf.writeShort(header.getSerial());
        buf.writeByte(header.getCmd());
        buf.writeBytes(header.getContent());
        buf.writeByte(header.getCheck());
        buf.writeBytes(header.getEnd());

        return buf.array();
    }

    public byte[] headerToSendBytes(byte[] content, int cmd, M2Header header) {

        header.setLength(14 + content.length);
        header.setSerial(getMsgSerial());
        header.setCmd(cmd);
        header.setContent(content);

        byte[] bytes = headerToContent(header);
        byte check = Common.getCheck(bytes);

        header.setCheck(check);

        return headerToBytes(header);
    }

    public int getCmdId() {
        return this.cmdId;
    }

    public ICache getM2CMDCacheProvider() {
        return m2CMDCacheProvider;
    }

    @Override
    public void parse(byte[] content, Header header) {

    }

    @Override
    public byte[] pack(String id, Header header) {
        return new byte[0];
    }


    /**
     * 命令序号
     **/
    private static AtomicLong msgSerial = new AtomicLong(0);

    protected static int getMsgSerial() {
        Long serial = msgSerial.incrementAndGet();
        if (serial > 65535) {
            msgSerial.set(0);
            serial = msgSerial.incrementAndGet();
        }

        return serial.intValue();
    }

    protected final static List<Integer> ACK_CMDS = new ArrayList<Integer>() {{
        this.add(0x01);
        this.add(0x03);
        this.add(0x04);
        this.add(0x05);
        this.add(0x06);
        this.add(0x09);
        this.add(0x0A);
    }};

    protected void put(String terminalId, int cmd, byte[] content) {

        MSGSenderTask.send(new SendMSG(terminalId, cmd, content));
    }

    protected void send(int respCmd, M2Header m2Header) {
        M2DataProcess process = (M2DataProcess) m2CMDCacheProvider.get(respCmd);
        byte[] respContent = process.pack(m2Header.getTerminalId(), m2Header);

        put(m2Header.getTerminalId(), respCmd, respContent);

        if (ACK_CMDS.contains(respCmd)) {
            waitACKCacheProvider.put(m2Header.getSerial(), new RepeatMSG(m2Header.getSerial(), new Date(), m2Header.getTerminalId(), respCmd, respContent));
        }
    }

    protected Position renderPosition(byte[] bytes) {

        if (bytes.length < 19) {
            logger.warn("长度不足，无法获取位置信息！");
        }

        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        long lat = buf.readUnsignedInt();
        long lng = buf.readUnsignedInt();
        int speed = buf.readByte();
        int direction = buf.readByte();
        byte[] heightBytes = new byte[2];
        buf.readBytes(heightBytes);
        int height = Common.renderHeight(heightBytes);
        byte[] status = new byte[4];
        buf.readBytes(status);

        Date dateTime = null;
        byte[] dateBytes = null;
        if (bytes.length == 19) {
            dateBytes = new byte[3];
        } else if (bytes.length == 22) {
            dateBytes = new byte[6];
        }
        buf.readBytes(dateBytes);
        dateTime = Common.bytesToDate(dateBytes);

        return new Position(lng, lat, speed, direction, height, status, dateTime);
    }

    protected class Position {
        private long lng;
        private long lat;
        private int speed;
        private int direction;
        private int height;
        private byte[] status;
        private Date dateTime;

        public Position() {
        }

        public Position(long lng, long lat, int speed, int direction, int height, byte[] status, Date dateTime) {
            this.lng = lng;
            this.lat = lat;
            this.speed = speed;
            this.direction = direction;
            this.height = height;
            this.status = status;
            this.dateTime = dateTime;
        }

        public long getLng() {
            return lng;
        }

        public void setLng(long lng) {
            this.lng = lng;
        }

        public long getLat() {
            return lat;
        }

        public void setLat(long lat) {
            this.lat = lat;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public byte[] getStatus() {
            return status;
        }

        public void setStatus(byte[] status) {
            this.status = status;
        }

        public Date getDateTime() {
            return dateTime;
        }

        public void setDateTime(Date dateTime) {
            this.dateTime = dateTime;
        }
    }
}
