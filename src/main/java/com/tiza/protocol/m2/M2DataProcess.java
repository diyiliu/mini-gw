package com.tiza.protocol.m2;

import com.tiza.protocol.IDataProcess;
import com.tiza.protocol.model.BackupMSG;
import com.tiza.protocol.model.SendMSG;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.entity.VehicleInfo;
import com.tiza.util.task.impl.MSGSenderTask;
import com.tiza.util.ws.impl.M2Sender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    protected ICache vehicleCacheProvider;

    @Resource
    private ICache monitorCacheProvider;

    protected int cmdId = 0xFF;

    @Override
    public M2Header dealHeader(byte[] bytes) {

        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        int length = buf.readUnsignedShort();

        byte[] termi = new byte[5];
        buf.readBytes(termi);
        String terminalId = CommonUtil.parseSIM(termi);

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
        buf.writeBytes(CommonUtil.packSIM(header.getTerminalId()));
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
        buf.writeBytes(CommonUtil.packSIM(header.getTerminalId()));
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
        byte check = CommonUtil.getCheck(bytes);

        header.setCheck(check);

        return headerToBytes(header);
    }

    public byte[] toSendBytes(int cmd, M2Header m2Header, Object... argus) {
        M2DataProcess process = (M2DataProcess) m2CMDCacheProvider.get(cmd);
        byte[] content = process.pack(m2Header, argus);

        return headerToSendBytes(content, cmd, m2Header);
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
    public byte[] pack(Header header, Object... argus) {
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

    protected final static List<Integer> ACK_CMDS = new ArrayList<Integer>() {
        {
            this.add(0x01);
            this.add(0x03);
            this.add(0x04);
            this.add(0x05);
            this.add(0x06);
            this.add(0x09);
            this.add(0x0A);
        }
    };

    protected void put(String terminalId, int cmd, byte[] content) {

        MSGSenderTask.send(new SendMSG(terminalId, cmd, content));
    }

    public void send(int cmd, M2Header m2Header) {
        M2DataProcess process = (M2DataProcess) m2CMDCacheProvider.get(cmd);
        byte[] content = process.pack(m2Header);

        if (ACK_CMDS.contains(cmd)) {
            waitACKCacheProvider.put(m2Header.getSerial(), new BackupMSG(m2Header.getSerial(), new Date(),
                    m2Header.getTerminalId(), cmd, content));
        }

        put(m2Header.getTerminalId(), cmd, content);
    }

    public void send(int cmd, M2Header m2Header, int id, Object... argus) {
        M2DataProcess process = (M2DataProcess) m2CMDCacheProvider.get(cmd);
        byte[] content = process.pack(m2Header, argus);

        if (ACK_CMDS.contains(cmd)) {
            BackupMSG backupMSG = new BackupMSG(m2Header.getSerial(), new Date(),
                    m2Header.getTerminalId(), cmd, content);
            backupMSG.setId(id);

            waitACKCacheProvider.put(m2Header.getSerial(), backupMSG);
        }

        // 重点监控
        if (monitorCacheProvider.containsKey(m2Header.getTerminalId())) {
            logger.info("下发消息，终端[{}], 命令[{}], 原始数据[{}]", m2Header.getTerminalId(), CommonUtil.toHex(cmd), CommonUtil.bytesToString(content));
        }

        put(m2Header.getTerminalId(), cmd, content);

        StringBuilder strb = new StringBuilder();
        strb.append("UPDATE ").append(Constant.DBInfo.DB_CLOUD_USER).append(".").append(Constant.DBInfo.DB_CLOUD_INSTRUCTION)
                .append(" SET ResponseStatus=2 WHERE ID=").append(id);

        CommonUtil.dealToDb(strb.toString());
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
        int height = CommonUtil.renderHeight(heightBytes);
        byte[] statusBytes = new byte[4];
        buf.readBytes(statusBytes);
        long status = CommonUtil.bytesToLong(statusBytes);

        Date dateTime;
        byte[] dateBytes = null;
        if (bytes.length == 19) {
            dateBytes = new byte[3];
        } else if (bytes.length == 22) {
            dateBytes = new byte[6];
        }
        buf.readBytes(dateBytes);
        dateTime = CommonUtil.bytesToDate(dateBytes);

        return new Position(lng, lat, speed, direction, height, status, dateTime);
    }

    protected class Position {
        private Long lng;
        private Long lat;
        private Double lngD;
        private Double latD;

        private Integer speed;
        private Integer direction;
        private Integer height;
        private Long status;
        private Date dateTime;

        public Position() {
        }

        public Position(long lng, long lat, int speed, int direction, int height, long status, Date dateTime) {
            this.lng = lng;
            this.lat = lat;
            this.speed = speed;
            this.direction = direction;
            this.height = height;
            this.status = status;
            this.dateTime = dateTime;
        }

        public Long getLng() {
            return lng;
        }

        public void setLng(Long lng) {
            this.lng = lng;
        }

        public Long getLat() {
            return lat;
        }

        public void setLat(Long lat) {
            this.lat = lat;
        }

        public Double getLngD() {
            double d = this.lng / 1000000.0;
            lngD = CommonUtil.keepDecimal(d, 2);
            return lngD;
        }

        public void setLngD(Double lngD) {
            this.lngD = lngD;
        }

        public Double getLatD() {
            double d = this.lat / 1000000.0;
            latD = CommonUtil.keepDecimal(d, 2);
            return latD;
        }

        public void setLatD(Double latD) {
            this.latD = latD;
        }

        public Integer getSpeed() {
            return speed;
        }

        public void setSpeed(Integer speed) {
            this.speed = speed;
        }

        public Integer getDirection() {
            return direction;
        }

        public void setDirection(Integer direction) {
            this.direction = direction;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Long getStatus() {
            return status;
        }

        public void setStatus(Long status) {
            this.status = status;
        }

        public Date getDateTime() {
            return dateTime;
        }

        public void setDateTime(Date dateTime) {
            this.dateTime = dateTime;
        }
    }

    protected class Status {

        private int location;
        private int lat;
        private int lng;
        private int acc;
        private int lock;

        private int discontinue;
        private int powerOff;
        private int lowPower;
        private int changeSIM;
        private int gpsFault;
        private int loseAntenna;
        private int aerialCircuit;
        private int powerDefence;
        private int overSpeed;
        private int trailer;
        private int uncap;

        public int getLocation() {
            return location;
        }

        public void setLocation(int location) {
            this.location = location;
        }

        public int getLat() {
            return lat;
        }

        public void setLat(int lat) {
            this.lat = lat;
        }

        public int getLng() {
            return lng;
        }

        public void setLng(int lng) {
            this.lng = lng;
        }

        public int getAcc() {
            return acc;
        }

        public void setAcc(int acc) {
            this.acc = acc;
        }

        public int getLock() {
            return lock;
        }

        public void setLock(int lock) {
            this.lock = lock;
        }

        public int getDiscontinue() {
            return discontinue;
        }

        public void setDiscontinue(int discontinue) {
            this.discontinue = discontinue;
        }

        public int getPowerOff() {
            return powerOff;
        }

        public void setPowerOff(int powerOff) {
            this.powerOff = powerOff;
        }

        public int getLowPower() {
            return lowPower;
        }

        public void setLowPower(int lowPower) {
            this.lowPower = lowPower;
        }

        public int getChangeSIM() {
            return changeSIM;
        }

        public void setChangeSIM(int changeSIM) {
            this.changeSIM = changeSIM;
        }

        public int getGpsFault() {
            return gpsFault;
        }

        public void setGpsFault(int gpsFault) {
            this.gpsFault = gpsFault;
        }

        public int getLoseAntenna() {
            return loseAntenna;
        }

        public void setLoseAntenna(int loseAntenna) {
            this.loseAntenna = loseAntenna;
        }

        public int getAerialCircuit() {
            return aerialCircuit;
        }

        public void setAerialCircuit(int aerialCircuit) {
            this.aerialCircuit = aerialCircuit;
        }

        public int getPowerDefence() {
            return powerDefence;
        }

        public void setPowerDefence(int powerDefence) {
            this.powerDefence = powerDefence;
        }

        public int getOverSpeed() {
            return overSpeed;
        }

        public void setOverSpeed(int overSpeed) {
            this.overSpeed = overSpeed;
        }

        public int getTrailer() {
            return trailer;
        }

        public void setTrailer(int trailer) {
            this.trailer = trailer;
        }

        public int getUncap() {
            return uncap;
        }

        public void setUncap(int uncap) {
            this.uncap = uncap;
        }
    }

    protected int statusBit(long l, int offset) {

        return new Long((l >> offset) & 0x01).intValue();
    }

    protected Status renderStatu(long l) {
        Status status = new Status();

        status.setLocation(statusBit(l, 0));
        status.setLat(statusBit(l, 1));
        status.setLng(statusBit(l, 2));
        status.setAcc(statusBit(l, 3));
        status.setLock(statusBit(l, 4));
        status.setDiscontinue(statusBit(l, 8));
        status.setPowerOff(statusBit(l, 9));
        status.setLowPower(statusBit(l, 10));
        status.setChangeSIM(statusBit(l, 11));
        status.setGpsFault(statusBit(l, 12));
        status.setLoseAntenna(statusBit(l, 13));
        status.setAerialCircuit(statusBit(l, 14));
        status.setPowerDefence(statusBit(l, 15));
        status.setOverSpeed(statusBit(l, 16));
        status.setTrailer(statusBit(l, 17));
        status.setUncap(statusBit(l, 18));

        return status;
    }

    protected class Parameter {

        private Long accTime;
        private Double accHour;
        private Integer gsmSignal;
        private Double voltage;
        private Integer satellite;

        public Parameter() {

        }

        public Parameter(Long accTime, Integer gsmSignal, Double voltage, Integer satellite) {
            this.accTime = accTime;
            this.gsmSignal = gsmSignal;
            this.voltage = voltage;
            this.satellite = satellite;
        }

        public Long getAccTime() {
            return accTime;
        }

        public void setAccTime(Long accTime) {
            this.accTime = accTime;
        }

        public Double getAccHour() {
            if (accTime != null) {
                double d = accTime / 3600.0;
                accHour = CommonUtil.keepDecimal(d, 2);
            }
            return accHour;
        }

        public void setAccHour(Double accHour) {
            this.accHour = accHour;
        }

        public Integer getGsmSignal() {
            return gsmSignal;
        }

        public void setGsmSignal(Integer gsmSignal) {
            this.gsmSignal = gsmSignal;
        }

        public Double getVoltage() {
            return voltage;
        }

        public void setVoltage(Double voltage) {
            this.voltage = voltage;
        }

        public Integer getSatellite() {
            return satellite;
        }

        public void setSatellite(Integer satellite) {
            this.satellite = satellite;
        }
    }


    protected void toDB(String terminalId, Position position, Status status) {

        if (!vehicleCacheProvider.containsKey(terminalId)) {
            return;
        }

        VehicleInfo vehicle = (VehicleInfo) vehicleCacheProvider.get(terminalId);
        Date now = new Date();

        Map valueMap = new HashMap() {
            {
                this.put("Lat", position.getLatD());
                this.put("Lng", position.getLngD());
                //this.put("EncryptLat", );
                //this.put("EncryptLng", );
                this.put("Speed", position.getSpeed());
                this.put("Direction", position.getDirection());
                this.put("GpsTime", position.getDateTime());
                this.put("SystemTime", now);
                this.put("AccStatus", status.acc);
                this.put("LocationStatus", status.location);
                this.put("PowerOff", status.getPowerOff());
                this.put("LowVoltage", status.getLowPower());
                this.put("GpsModule", status.getGpsFault());
                this.put("GpsAntenna", status.getLoseAntenna());
            }
        };

        Map whereMap = new HashMap() {
            {
                this.put("VehicleId", vehicle.getId());
            }
        };
        // 插入轨迹表
        valueMap.put("VehicleId", vehicle.getId());
        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER,
                CommonUtil.monthTable(Constant.DBInfo.DB_CLOUD_VEHICLETRACK, now),
                valueMap);

        // 更新当前位置表
        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER,
                Constant.DBInfo.DB_CLOUD_VEHICLEGPSINFO,
                valueMap, whereMap);
    }

    protected void toDB(String terminalId, Position position, Status status, Parameter parameter) {

        if (!vehicleCacheProvider.containsKey(terminalId)) {
            return;
        }

        VehicleInfo vehicle = (VehicleInfo) vehicleCacheProvider.get(terminalId);
        Date now = new Date();

        Map valueMap = new HashMap() {
            {
                this.put("Lat", position.getLatD());
                this.put("Lng", position.getLngD());
                //this.put("EncryptLat", );
                //this.put("EncryptLng", );
                this.put("Speed", position.getSpeed());
                this.put("Direction", position.getDirection());
                this.put("GpsTime", position.getDateTime());
                this.put("SystemTime", now);
                this.put("AccStatus", status.acc);
                this.put("LocationStatus", status.location);
                this.put("PowerOff", status.getPowerOff());
                this.put("LowVoltage", status.getLowPower());
                this.put("GpsModule", status.getGpsFault());
                this.put("GpsAntenna", status.getLoseAntenna());

                if (parameter.getGsmSignal() != null) {
                    this.put("GsmSignal", parameter.getGsmSignal());
                }
                if (parameter.getSatellite() != null) {

                    this.put("GpsSatellite", parameter.getSatellite());
                }
                if (parameter.getAccHour() != null) {

                    this.put("AccOnHours", parameter.getAccHour());
                }

            }
        };

        Map whereMap = new HashMap() {
            {
                this.put("VehicleId", vehicle.getId());
            }
        };

        // 插入轨迹表
        valueMap.put("VehicleId", vehicle.getId());
        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER,
                CommonUtil.monthTable(Constant.DBInfo.DB_CLOUD_VEHICLETRACK, now),
                valueMap);

        valueMap.put("WorkDataTime", position.getDateTime());
        // 更新当前位置表
        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER,
                Constant.DBInfo.DB_CLOUD_VEHICLEGPSINFO,
                valueMap, whereMap);
    }

}
