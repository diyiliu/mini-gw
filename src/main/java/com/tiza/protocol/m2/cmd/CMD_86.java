package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.BackupMSG;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import com.tiza.util.CommonUtil;
import com.tiza.util.config.Constant;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: CMD_86
 * Author: DIYILIU
 * Update: 2016-03-29 9:27
 */

@Service
public class CMD_86 extends M2DataProcess {

    public CMD_86() {
        this.cmdId = 0x86;
    }

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        String key = m2Header.getTerminalId() + CommonUtil.toHex(cmdId);
        if (matchACKCacheProvider.containsKey(key)) {
            BackupMSG backupMSG = (BackupMSG) matchACKCacheProvider.get(key);
            int id = backupMSG.getId();
            int cmd = backupMSG.getCmd();

            toDB(id, cmd, content[0]);
        }

        send(0x02, m2Header);
    }

    public void toDB(int id, int cmd, int result) {

        Map valueMap = new HashMap() {
            {
                this.put("ResponseStatus", result == 0 ? 11 : 12);
                this.put("ResponseDate", new Date());
            }
        };

        Map whereMap = new HashMap() {
            {
                this.put("Id", id);
                //this.put("ParamId", cmd);
            }
        };

        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER, Constant.DBInfo.DB_CLOUD_INSTRUCTION, valueMap, whereMap);
    }
}
