package com.tiza.protocol.mobile.cmd;

import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.MobileHeader;
import com.tiza.util.CommonUtil;
import com.tiza.util.config.Constant;
import com.tiza.util.entity.VehicleInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_86
 * Author: DIYILIU
 * Update: 2016-04-14 11:13
 */

@Service
public class TAG_86 extends MobileDataProcess {

    public TAG_86() {
        this.cmdId = 0x86;
    }

    @Override
    public void parse(byte[] content, Header header) {
        MobileHeader mobileHeader = (MobileHeader) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        byte mode = buf.readByte();
        int mmm = mode & 0x07;

        int day = buf.readUnsignedByte();
        int hour = buf.readUnsignedByte();
        int minute = buf.readUnsignedByte();
        int maxWorktime = buf.readUnsignedShort();

        toDB(mobileHeader.getDevIMEI(), mmm);
    }

    private void toDB(String terminalId, int mode) {

        if (!vehicleCacheProvider.containsKey(terminalId)){
            return;
        }
        VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(terminalId);

        StringBuilder strb = new StringBuilder("UPDATE ");
        strb.append(Constant.DBInfo.DB_CLOUD_VEHICLEGPSINFO)
                .append(" SET WorkMode=").append(mode).append(" ")
                .append( "WHERE VehicleId=").append(vehicleInfo.getId());

        CommonUtil.dealToDb(strb.toString());
    }
}
