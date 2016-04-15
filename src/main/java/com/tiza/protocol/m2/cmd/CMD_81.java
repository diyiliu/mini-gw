package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.header.Header;
import org.springframework.stereotype.Service;

/**
 * Description: CMD_81
 * Author: DIYILIU
 * Update: 2016-03-21 16:23
 */

@Service
public class CMD_81 extends M2DataProcess{

    public CMD_81() {
        this.cmdId = 0x81;
    }

    @Override
    public void parse(byte[] content, Header header) {

    }
}
