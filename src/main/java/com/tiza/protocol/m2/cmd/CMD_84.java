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
import java.util.*;

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

    @Resource
    private ICache matchACKCacheProvider;

    @Override
    public void parse(byte[] content, Header header) {
        M2Header m2Header = (M2Header) header;

        ByteBuf buf = Unpooled.copiedBuffer(content);
        if (buf.readableBytes() < 3) {
            return;
        }
        int result = buf.readByte();
        int paramId = buf.readUnsignedShort();

        String key = m2Header.getTerminalId() + CommonUtil.toHex(0x07) + CommonUtil.toHex(paramId);
        if (matchACKCacheProvider.containsKey(key)) {
            BackupMSG backupMSG = (BackupMSG) matchACKCacheProvider.get(key);
            int id = backupMSG.getId();
            int cmd = backupMSG.getCmd();

            String value = null;
            if (result == 0) {
                int length = buf.readByte();
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                value = toValue(paramId, bytes);

                //logger.info("终端参数ID[{}]，参数值[{}], 原始值[{}]", CommonUtil.toHex(paramId), value, CommonUtil.bytesToString(bytes));
            }
            matchACKCacheProvider.remove(key);

            toDB(id, cmd, result, value);
        }

        send(0x02, m2Header);
    }

    private String toValue(int paramId, byte[] bytes) {

        String value;
        switch (paramId) {
            case 0x06:
                value = CommonUtil.bytesToIp(bytes);
                break;
            case 0x08:
                value = CommonUtil.bytesToIp(bytes);
                break;
            case 0x0A:
                value = CommonUtil.bytesToLong(bytes) + "";
                break;
            case 0x0B:
                value = (int) bytes[0] + "";
                break;
            case 0x0D:
                value = CommonUtil.bytesToLong(bytes) + "";
                break;
            case 0x0E:
                value = CommonUtil.bytesToLong(new byte[]{bytes[0], bytes[1]}) +
                        "," + CommonUtil.bytesToLong(new byte[]{bytes[2], bytes[3]});
                break;
            case 0x0F:
                value = CommonUtil.bytesToLong(bytes) + "";
                break;
            case 0x10:
                value = CommonUtil.bytesToLong(bytes) + "";
                break;
            default:
                value = new String(bytes);
        }

        return value;
    }

    public void toDB(int id, int cmd, int result, String value) {

        Map valueMap = new HashMap() {
            {
                this.put("ResponseStatus", result == 0 ? 11 : 12);
                this.put("ResponseDate", new Date());
                if (value != null) {
                    this.put("ResponseData", value);
                }
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
