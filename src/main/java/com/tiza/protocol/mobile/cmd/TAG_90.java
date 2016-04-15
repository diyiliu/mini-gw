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
 * Description: TAG_90
 * Author: DIYILIU
 * Update: 2016-04-14 11:14
 */

@Service
public class TAG_90 extends MobileDataProcess {

    public TAG_90() {
        this.cmdId = 0x90;
    }

    @Override
    public void parse(byte[] content, Header header) {
        MobileHeader mobileHeader = (MobileHeader) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);

        long time = buf.readUnsignedInt();
        long worktime = buf.readUnsignedInt();
        int cnt = buf.readUnsignedShort();
        int creg = buf.readUnsignedByte();
        int attach = buf.readUnsignedByte();
        int signal = buf.readUnsignedByte();

        toDB(mobileHeader.getDevIMEI(), signal);
    }

    private void toDB(String terminalId, int signal) {

        if (!vehicleCacheProvider.containsKey(terminalId)){
            return;
        }
        VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(terminalId);

        StringBuilder strb = new StringBuilder("UPDATE ");
        strb.append(Constant.DBInfo.DB_CLOUD_VEHICLEGPSINFO)
                .append(" SET GsmSignal=").append(signal).append(" ")
                .append( "WHERE VehicleId=").append(vehicleInfo.getId());

        CommonUtil.dealToDb(strb.toString());
    }
}
