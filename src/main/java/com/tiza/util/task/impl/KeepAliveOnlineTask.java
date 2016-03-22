package com.tiza.util.task.impl;

import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.util.DateUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Description: KeepAliveOnlineTask
 * Author: DIYILIU
 * Update: 2016-03-22 9:08
 */

public class KeepAliveOnlineTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /** 消息间隔 **/
    private final static int MSG_INTERVAL = 90;
    /** 消息延时 **/
    private final static int MSG_DELAY = 2;

    @Resource
    private ICache onlineCacheProvider;

    @Override
    public void execute() {
        Date now = new Date();
        logger.info("检测在线...");

        Set<Object> keys = onlineCacheProvider.getKeys();

        for (Iterator iter = keys.iterator(); iter.hasNext();){

            Object key = iter.next();
            MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(key);
            Date receiveTime = pipeline.getReceiveTime();

            if (now.getTime() - receiveTime.getTime() > (MSG_INTERVAL + MSG_DELAY) * 1000){
                logger.info("终端离线[{}], 检测时间[{}]", key, DateUtil.dateToString(now));
                onlineCacheProvider.remove(key);
            }
        }
    }

}
