package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_89
 * Author: DIYILIU
 * Update: 2016-03-29 9:34
 */

@Service
public class CMD_89 extends M2DataProcess {

    public CMD_89() {
        this.cmdId = 0x89;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        Position position = renderPosition(content);
        Status status = renderStatu(position.getStatus());

        toDB(m2Header.getTerminalId(), position, status);

        send(0x02, m2Header);
    }
}
