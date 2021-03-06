package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_80
 * Author: DIYILIU
 * Update: 2016-03-17 15:15
 */

@Service
public class CMD_80 extends M2DataProcess {

    public CMD_80() {
        this.cmdId = 0x80;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        Position position = renderPosition(content);
        Status status = renderStatu(position.getStatus());

        toDB(m2Header.getTerminalId(), position, status);

        send(0x01, m2Header);
    }
}