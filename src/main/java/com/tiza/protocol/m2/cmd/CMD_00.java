package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_00
 * Author: DIYILIU
 * Update: 2016-03-22 10:03
 */

@Service
public class CMD_00 extends M2DataProcess {

    public CMD_00() {
        this.cmdId = 0x00;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        byte[] bytes = new byte[0];

        return headerToSendBytes(bytes, this.cmdId, m2Header);
    }
}
