package com.tiza.util.ws.impl;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.header.M2Header;
import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.util.cache.ICache;
import com.tiza.util.ws.IM2Sender;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

/**
 * Description: M2Sender
 * Author: DIYILIU
 * Update: 2016-03-29 15:54
 */

@WebService
public class M2Sender implements IM2Sender {

    private String address = "http://192.168.1.19:8989/cloud/m2sender";

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private M2DataProcess m2DataProcess;

    @Override
    public void locate(int id, String terminalId, int interval) {

        if (onlineCacheProvider.containsKey(terminalId)) {
            MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(terminalId);
            M2Header m2Header = (M2Header) pipeline.getHeader();

            m2DataProcess.send(0x03, m2Header, id, interval);
        }
    }

    @Override
    public void init() {

        Endpoint.publish(address, this);
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
