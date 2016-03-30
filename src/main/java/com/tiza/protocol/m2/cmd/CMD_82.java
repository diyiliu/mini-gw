package com.tiza.protocol.m2.cmd;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.BackupMSG;
import com.tiza.protocol.model.header.Header;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Resource
    private ICache waitACKCacheProvider;

    @Override
    public void parse(byte[] content, Header header) {

        ByteBuf buf = Unpooled.copiedBuffer(content);
        int serial = buf.readUnsignedShort();
        int cmd = buf.readByte();
        int result = buf.readByte();

        if (waitACKCacheProvider.containsKey(serial)){
            //logger.info("收到应答，序号[{}], 命令[{}], 结果[{}], 车辆[{}]", serial, CommonUtil.toHex(cmd), result, m2Header.getTerminalId());
            BackupMSG backupMSG = (BackupMSG) waitACKCacheProvider.get(serial);
            int id = backupMSG.getId();

            if (id > 0){
                toDB(id, cmd, result);
            }

            waitACKCacheProvider.remove(serial);
        }
    }

    public void toDB(int id, int cmd, int result){

        Map valueMap = new HashMap(){
            {
                this.put("ResponseStatus", result == 0? 11: 12);
                this.put("ResponseDate", new Date());
            }
        };

        Map whereMap = new HashMap(){
            {
                this.put("Id", id);
                //this.put("ParamId", cmd);
            }
        };

        CommonUtil.dealToDb(Constant.DBInfo.DB_CLOUD_USER, Constant.DBInfo.DB_CLOUD_INSTRUCTION, valueMap, whereMap);
    }
}
