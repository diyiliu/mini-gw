package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_06
 * Author: DIYILIU
 * Update: 2016-04-11 14:37
 */

@Service
public class CMD_06 extends M2DataProcess {

    public CMD_06() {
        this.cmdId = 0x06;
    }

    @Override
    public byte[] pack(Header header, Object... argus) {
        M2Header m2Header = (M2Header) header;

        String url = (String) argus[0];
        int length = url.getBytes().length;

        byte[] content = new byte[length + 1];
        content[0] = 0;

        System.arraycopy(url.getBytes(), 0, content, 1, length);

        return headerToSendBytes(content, this.cmdId, m2Header);
    }
}
