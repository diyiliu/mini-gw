package com.tiza.util.task.impl;

import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.util.cache.ICache;
import com.tiza.util.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Set;

/**
 * Description: HeartbeatSenderTask
 * Author: DIYILIU
 * Update: 2016-03-22 9:11
 */
public class HeartbeatSenderTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static int HEARTBEAT_CMD = 0x00;

    @Resource
    protected ICache m2CMDCacheProvider;

    @Resource
    private ICache onlineCacheProvider;

    @Override
    public void execute() {
        logger.info("发送心跳...");

        Set<Object> keys = onlineCacheProvider.getKeys();

        M2DataProcess m2Process = (M2DataProcess) m2CMDCacheProvider.get(HEARTBEAT_CMD);
        for (Iterator iter = keys.iterator(); iter.hasNext();){
            Object key = iter.next();
            MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(key);

            String terminal = (String) key;
            byte[] bytes = m2Process.pack(terminal, pipeline.getHeader());
            pipeline.send(terminal, bytes);
        }
    }
}
