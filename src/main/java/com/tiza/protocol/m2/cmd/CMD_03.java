package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import com.tiza.util.CommonUtil;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_03
 * Author: DIYILIU
 * Update: 2016-03-29 16:42
 */

@Service
public class CMD_03 extends M2DataProcess{

    public CMD_03() {
        this.cmdId = 0x03;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        int interval = (int) argus[0];
        byte[] bytes = CommonUtil.longToBytes(interval, 2);

        return headerToSendBytes(bytes, cmdId, m2Header);
    }
}
