package com.tiza.util.task.impl;

import com.tiza.protocol.IDataProcess;
import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.model.SendMSG;
import com.tiza.model.header.Header;
import com.tiza.model.header.M2Header;
import com.tiza.model.pipeline.MSGPipeline;
import com.tiza.util.cache.ICache;
import com.tiza.util.task.ITask;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Set;

/**
 * Description: HeartbeatSenderTask
 * Author: DIYILIU
 * Update: 2016-03-22 9:11
 */
public class HeartbeatSenderTask implements ITask {

    private final static int M2_HEARTBEAT_CMD = 0x00;

    @Resource
    protected ICache m2CMDCacheProvider;

    @Resource
    private ICache onlineCacheProvider;

    @Override
    public void execute() {
        Set<Object> keys = onlineCacheProvider.getKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            Object key = iter.next();

            MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(key);

            String terminal = (String) key;
            Header header = pipeline.getHeader();

            if (header instanceof M2Header) {
                IDataProcess process = (M2DataProcess) m2CMDCacheProvider.get(M2_HEARTBEAT_CMD);
                byte[] bytes = process.pack(header);
                MSGSenderTask.send(new SendMSG(terminal, M2_HEARTBEAT_CMD, bytes));
            }
        }
    }
}
