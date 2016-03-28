package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.JacksonUtil;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_83
 * Author: DIYILIU
 * Update: 2016-03-21 16:21
 */

@Service
public class CMD_83 extends M2DataProcess{

    public CMD_83() {
        this.cmdId = 0x83;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        Position position = renderPosition(content);

        Status status = renderStatu(position.getStatus());

        toDB(m2Header.getTerminalId(), position, status);
    }
}
