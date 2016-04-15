package com.tiza.protocol.mobile.cmd;

import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.MobileHeader;
import com.tiza.util.CommonUtil;
import com.tiza.util.config.Constant;
import com.tiza.util.entity.VehicleInfo;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_85
 * Author: DIYILIU
 * Update: 2016-04-14 11:13
 */

@Service
public class TAG_85 extends MobileDataProcess {

    public TAG_85() {
        this.cmdId = 0x85;
    }

    @Override
    public void parse(byte[] content, Header header) {
        MobileHeader mobileHeader = (MobileHeader) header;

        String softVersion = new String(content);

        toDB(mobileHeader.getDevIMEI(), softVersion);
    }

    private void toDB(String terminalId, String softVersion) {

        if (!vehicleCacheProvider.containsKey(terminalId)){
            return;
        }
        VehicleInfo vehicleInfo = (VehicleInfo) vehicleCacheProvider.get(terminalId);

        StringBuilder strb = new StringBuilder("UPDATE ");
        strb.append(Constant.DBInfo.DB_CLOUD_VEHICLEGPSINFO)
                .append(" SET GpsVersion='").append(softVersion).append("' ")
                .append( "WHERE VehicleId=").append(vehicleInfo.getId());

        CommonUtil.dealToDb(strb.toString());
    }
}
