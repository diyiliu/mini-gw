package com.tiza.protocol.mobile.cmd;

import com.tiza.model.header.Header;
import com.tiza.model.header.MobileHeader;
import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.util.CommonUtil;
import com.tiza.util.HttpclientUtil;
import com.tiza.util.JacksonUtil;
import com.tiza.util.bean.Bts;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Description: TAG_83
 * Author: DIYILIU
 * Update: 2016-04-14 10:21
 */

@Service
public class TAG_83 extends MobileDataProcess {

    public TAG_83() {
        this.cmdId = 0x83;
    }

    @Override
    public void parse(byte[] content, Header header) {
        MobileHeader mobileHeader = (MobileHeader) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        int mcc = buf.readUnsignedShort();
        int mnc = buf.readUnsignedByte();
        int cellNum = buf.readUnsignedByte();


        int lac = 0;
        int ci = 0;
        int signal = 0;
        StringBuilder cl = new StringBuilder();
        for (int i = 0; i < cellNum; i++) {
            if (buf.readableBytes() < 5) {
                break;
            }
            int lac_x = buf.readUnsignedShort();
            int cellId_x = buf.readUnsignedShort();
            int signal_x = buf.readUnsignedByte();
            if (signal_x > signal) {
                signal = signal_x;
                lac = lac_x;
                ci = cellId_x;
            }
            cl.append(mcc).append(",").append(mnc).append(",")
                    .append(lac_x).append(",").append(cellId_x).append(",").append(signal_x).append(";");
            //logger.info("小区编号:{}, 基站编号:{}, 信号强度:{}", lac, cellId, signal);
        }

        /**
         String url = "http://api.cellocation.com/cell/";
         Map params = new HashedMap();
         params.put("mcc", mcc);
         params.put("mnc", mnc);
         params.put("lac", lac);
         params.put("ci", ci);
         params.put("coord", "wgs84");
         params.put("output", "json");
         */

        String url = "http://api.cellocation.com/loc/";

        Map params = new HashedMap();
        params.put("cl", cl.substring(0, cl.length() - 1));
        params.put("coord", "wgs84");
        params.put("output", "json");

        String result = null;
        try {
            result = HttpclientUtil.askFor(url, "GET", params);
        } catch (Exception e) {
            logger.error("请求基站数据失败！,{}", e.getMessage());
        }

        if (CommonUtil.isEmpty(result)) {
            return;
        }

        Bts bts = null;
        try {
            bts = JacksonUtil.toObject(result, Bts.class);
        } catch (IOException e) {
            logger.error("JSON格式转换错误！,[{},{}]", result, Bts.class.getName());
        }

        if (bts == null || bts.getErrcode() > 0) {
            logger.error("基站数据请求失败！, errcode[{}]", bts.getErrcode());
            return;
        }

        Position position = new Position();
        position.setLocate(1);
        position.setLatD(Double.valueOf(bts.getLat()));
        position.setLngD(Double.valueOf(bts.getLon()));
        // 基站定位
        position.setMode(2);

        toDB(mobileHeader.getDevIMEI(), position);
    }
}
