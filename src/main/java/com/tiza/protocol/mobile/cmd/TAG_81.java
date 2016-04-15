package com.tiza.protocol.mobile.cmd;

import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.model.header.Header;
import com.tiza.model.header.MobileHeader;
import org.springframework.stereotype.Service;

/**
 * Description: TAG_81
 * Author: DIYILIU
 * Update: 2016-04-14 13:40
 */

@Service
public class TAG_81 extends MobileDataProcess {

    public TAG_81() {
        this.cmdId = 0x81;
    }

    @Override
    public void parse(byte[] content, Header header) {
        MobileHeader mobileHeader = (MobileHeader) header;

        Position position = renderPosition(content);

        toDB(mobileHeader.getDevIMEI(), position);
    }
}
