package com.tiza.protocol.mobile.cmd;

import com.tiza.model.header.Header;
import com.tiza.model.header.MobileHeader;
import com.tiza.protocol.mobile.MobileDataProcess;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_0A
 * Author: DIYILIU
 * Update: 2016-04-15 11:08
 */

@Service
public class TAG_0A extends MobileDataProcess {

    public TAG_0A() {
        this.cmdId = 0x0A;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {

        String value = (String) argus[0];
        byte[] content = new byte[]{(byte) Integer.parseInt(value, 16)};

        return toTLV(content, cmdId);
    }
}
