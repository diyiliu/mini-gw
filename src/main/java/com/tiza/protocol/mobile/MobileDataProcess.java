package com.tiza.protocol.mobile;

import com.tiza.protocol.IDataProcess;
import com.tiza.model.SendMSG;
import com.tiza.model.Tlv;
import com.tiza.model.header.Header;
import com.tiza.model.header.MobileHeader;
import com.tiza.util.CommonUtil;
import com.tiza.util.GpsCorrectUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.entity.VehicleInfo;
import com.tiza.util.task.impl.MSGSenderTask;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Description: MobileDataProcess
 * Author: DIYILIU
 * Update: 2016-04-13 11:22
 */

@Service
public class MobileDataProcess implements IDataProcess {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache mobileCMDCacheProvider;

    @Resource
    protected ICache vehicleCacheProvider;

    @Resource
    protected ICache waitACKCacheProvider;

    protected int cmdId = 0xFF;

    @Override
    public void init() {
        mobileCMDCacheProvider.put(cmdId, this);
    }

    @Override
    public Header dealHeader(byte[] bytes) {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        byte[] devBytes = new byte[8];
        buf.readBytes(devBytes);
        String devIMEI = CommonUtil.parseIMEI(devBytes);

        int cmd = buf.readUnsignedByte();
        short atrr = buf.readShort();
        int length = atrr & 0x3FF;

        MobileHeader mobileHeader = new MobileHeader(cmd, devIMEI, length);

        List<Tlv> tlvList = new ArrayList<>();
        while (buf.readableBytes() > 1) {
            int tag = buf.readUnsignedByte();
            int len = buf.readUnsignedByte();

            byte[] value = new byte[len];
            buf.readBytes(value);
            tlvList.add(new Tlv(tag, length, value));
        }

        mobileHeader.setContent(tlvList);

        return mobileHeader;
    }

    @Override
    public void parse(byte[] content, Header header) {
        MobileHeader mobileHeader = (MobileHeader) header;

        List<Tlv> tlvList = mobileHeader.getContent();
        for (Tlv tlv : tlvList) {
            int tag = tlv.getTag();
            int value = tlv.getValue()[0];

            Map valueMap = new HashMap() {
                {
                    this.put("ResponseStatus", value == 0x00? 11: 12);
                    this.put("ResponseDate", new Date());
                }
            };

            Map whereMap = new HashMap() {
                {
                    this.put("ResponseStatus", 1);
                    this.put("DeviceId", mobileHeader.getDevIMEI());
                    this.put("ParamId", tag);
                }
            };

            // 更新数据库
            CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER, Constant.DBInfo.DB_CLOUD_INSTRUCTION, valueMap, whereMap);

            waitACKCacheProvider.remove(mobileHeader.getDevIMEI() + tag);
        }
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        return new byte[0];
    }

    public ICache getMobileCMDCacheProvider() {
        return mobileCMDCacheProvider;
    }


    protected byte[] toTLV(byte[] bytes, int tag){

        ByteBuf buf = Unpooled.buffer(bytes.length + 2);
        buf.writeByte(tag);
        buf.writeByte(bytes.length);
        buf.writeBytes(bytes);

        return buf.array();
    }

    protected class Position {

        private int mode;
        private int locate;
        private int la;
        private int lo;
        private int em;

        private Long lng;
        private Long lat;
        private Double lngD;
        private Double latD;
        private Double enLngD;
        private Double enLatD;

        private Integer speed;
        private Integer direction;
        private Integer height;

        public Position() {
        }

        public Position(int mode, int locate, int la, int lo, int em,
                        Long lng, Long lat, Integer speed, Integer direction, Integer height) {
            this.mode = mode;
            this.locate = locate;
            this.la = la;
            this.lo = lo;
            this.em = em;
            this.lng = lng;
            this.lat = lat;
            this.speed = speed;
            this.direction = direction;
            this.height = height;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public int getLocate() {
            return locate;
        }

        public void setLocate(int locate) {
            this.locate = locate;
        }

        public int getLa() {
            return la;
        }

        public void setLa(int la) {
            this.la = la;
        }

        public int getLo() {
            return lo;
        }

        public void setLo(int lo) {
            this.lo = lo;
        }

        public int getEm() {
            return em;
        }

        public void setEm(int em) {
            this.em = em;
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
            lngD = CommonUtil.keepDecimal(d, 6);
            return lngD;
        }

        public Double getLatD() {
            double d = this.lat / 1000000.0;
            latD = CommonUtil.keepDecimal(d, 6);
            return latD;
        }

        public Double getEnLngD() {
            if (em == 0) {
                return CommonUtil.keepDecimal(GpsCorrectUtil.transform(latD, lngD).getLng(), 6);
            }

            return lngD;
        }

        public Double getEnLatD() {
            if (em == 0) {
                return CommonUtil.keepDecimal(GpsCorrectUtil.transform(latD, lngD).getLat(), 6);
            }

            return latD;
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
    }

    protected Position renderPosition(byte[] bytes) {

        if (bytes.length < 13) {
            logger.error("长度不足，无法获取位置信息！");
            return null;
        }

        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        byte status = buf.readByte();

        int mode = (status >> 6) & 0x03;
        int locate = (status >> 5) & 0x01;
        int la = (status >> 4) & 0x01;
        int lo = (status >> 3) & 0x01;
        int em = (status >> 2) & 0x01;
        long lat = buf.readUnsignedInt();
        long lng = buf.readUnsignedInt();
        byte[] heightBytes = new byte[2];
        buf.readBytes(heightBytes);
        int height = CommonUtil.renderHeight(heightBytes);
        int speed = buf.readUnsignedByte();
        int direction = buf.readUnsignedByte();

        return new Position(mode, locate, la, lo, em, lng, lat, speed, direction, height);
    }

    protected void toDB(String terminalId, Position position) {

        if (!vehicleCacheProvider.containsKey(terminalId)) {
            return;
        }

        VehicleInfo vehicle = (VehicleInfo) vehicleCacheProvider.get(terminalId);
        Date now = new Date();

        Map valueMap = new HashMap() {
            {
                this.put("Lat", position.getLatD());
                this.put("Lng", position.getLngD());
                this.put("EncryptLat", position.getEnLatD());
                this.put("EncryptLng", position.getEnLngD());
                this.put("Speed", position.getSpeed());
                this.put("Direction", position.getDirection());
                this.put("GpsTime", now);
                this.put("SystemTime", now);
                this.put("LocationStatus", position.getLocate());
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

        valueMap.put("LocationMode", position.getMode());
        // 更新当前位置表
        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER,
                Constant.DBInfo.DB_CLOUD_VEHICLEGPSINFO,
                valueMap, whereMap);
    }
}
