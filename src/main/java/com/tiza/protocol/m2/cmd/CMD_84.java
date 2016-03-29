package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_84
 * Author: DIYILIU
 * Update: 2016-03-29 9:26
 */

@Service
public class CMD_84 extends M2DataProcess {

    public CMD_84() {
        this.cmdId = 0x84;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        // TODO: 2016/3/29

        send(0x02, m2Header);
    }
}
