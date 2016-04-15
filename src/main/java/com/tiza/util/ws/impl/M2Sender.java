package com.tiza.util.ws.impl;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.BackupMSG;
import com.tiza.model.header.M2Header;
import com.tiza.model.pipeline.MSGPipeline;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.ws.IM2Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.util.Date;

/**
 * Description: M2Sender
 * Author: DIYILIU
 * Update: 2016-03-29 15:54
 */

@WebService
public class M2Sender implements IM2Sender {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String address = "http://192.168.1.19:8989/cloud/m2sender";

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private ICache matchACKCacheProvider;

    @Resource
    private M2DataProcess m2DataProcess;


    @Override
    public void locate(int id, String terminalId, int interval) {

        if (!onlineCacheProvider.containsKey(terminalId)) {
            logger.error("车辆[{}]离线，[定位]命令下发失败！", terminalId);
            return;
        }

        MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(terminalId);
        M2Header m2Header = (M2Header) pipeline.getHeader();

        m2DataProcess.send(0x03, m2Header, id, interval);
    }

    @Override
    public void setParam(int id, String terminalId, int paramId, Object... paramValue) {

        if (!onlineCacheProvider.containsKey(terminalId)) {
            logger.error("车辆[{}]离线，[设置参数]命令下发失败！", terminalId);
            return;
        }

        MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(terminalId);
        M2Header m2Header = (M2Header) pipeline.getHeader();

        m2DataProcess.send(0x04, m2Header, id, paramId, paramValue);
    }

    @Override
    public void queryParam(int id, String terminalId, int paramId) {
        if (!onlineCacheProvider.containsKey(terminalId)) {
            logger.error("车辆[{}]离线，[查询参数]命令下发失败！", terminalId);
            return;
        }
        int cmd = 0x07;

        MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(terminalId);
        M2Header m2Header = (M2Header) pipeline.getHeader();

        BackupMSG backupMSG = new BackupMSG(m2Header.getSerial(), new Date(),
                m2Header.getTerminalId(), cmd, m2DataProcess.toSendBytes(cmd, m2Header, paramId),
                Constant.Protocol.M2_REPEAT_COUNT, Constant.Protocol.M2_REPEAT_TIME);
        backupMSG.setId(id);

        String key = terminalId + CommonUtil.toHex(cmd) + CommonUtil.toHex(paramId);

        matchACKCacheProvider.put(key, backupMSG);

        m2DataProcess.send(cmd, m2Header, id, paramId);
    }

    @Override
    public void update(int id, String terminalId, String url) {
        if (!onlineCacheProvider.containsKey(terminalId)) {
            logger.error("车辆[{}]离线，[查询参数]命令下发失败！", terminalId);
            return;
        }
        int cmd = 0x06;

        MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(terminalId);
        M2Header m2Header = (M2Header) pipeline.getHeader();

        BackupMSG backupMSG = new BackupMSG(m2Header.getSerial(), new Date(),
                m2Header.getTerminalId(), cmd, m2DataProcess.toSendBytes(cmd, m2Header, url),
                Constant.Protocol.M2_REPEAT_COUNT, Constant.Protocol.M2_REPEAT_TIME);
        backupMSG.setId(id);

        String key = terminalId + CommonUtil.toHex(cmd);

        matchACKCacheProvider.put(key, backupMSG);

        m2DataProcess.send(cmd, m2Header, id, url);
    }

    @Override
    public void init() {

        Endpoint.publish(address, this);
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
