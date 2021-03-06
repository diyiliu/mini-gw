package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.BackupMSG;
import com.tiza.model.header.Header;
import com.tiza.util.CommonUtil;
import com.tiza.util.config.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: CMD_82
 * Author: DIYILIU
 * Update: 2016-03-21 14:51
 */

@Service
public class CMD_82 extends M2DataProcess {

    public CMD_82() {
        this.cmdId = 0x82;
    }

    @Override
    public void parse(byte[] content, Header header) {

        ByteBuf buf = Unpooled.copiedBuffer(content);
        int serial = buf.readUnsignedShort();
        int cmd = buf.readByte();
        int result = buf.readByte();

        if (waitACKCacheProvider.containsKey(serial)) {
            //logger.info("收到应答，序号[{}], 命令[{}], 结果[{}], 车辆[{}]", serial, CommonUtil.toHex(cmd), result, m2Header.getTerminalId());
            BackupMSG backupMSG = (BackupMSG) waitACKCacheProvider.get(serial);
            int id = backupMSG.getId();

            if (id > 0) {
                toDB(id, cmd, result);
            }

            waitACKCacheProvider.remove(serial);
        }
    }

    public void toDB(int id, int cmd, int result) {

        int status = 0;
        if (result == 0) {
            status = 11;
            if (isDoubleACK(cmd)) {
                status = 2;
            }
        } else if (result == 1) {
            status = 12;
        } else {
            logger.warn("82H,处理结果异常！[{}]", result);
        }

        final int rs = status;
        Map valueMap = new HashMap() {
            {
                this.put("ResponseStatus", rs);
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
