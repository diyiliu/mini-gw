package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.Common;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_85
 * Author: DIYILIU
 * Update: 2016-03-21 15:16
 */

@Service
public class CMD_85 extends M2DataProcess{

    public CMD_85() {
        this.cmdId = 0x85;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        send(0x02, m2Header);
    }
}
