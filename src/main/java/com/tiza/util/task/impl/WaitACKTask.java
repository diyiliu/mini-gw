package com.tiza.util.task.impl;

import com.tiza.protocol.model.RepeatMSG;
import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.util.Common;
import com.tiza.util.cache.ICache;
import com.tiza.util.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Description: WaitACKTask
 * Author: DIYILIU
 * Update: 2016-03-22 9:17
 */
public class WaitACKTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache waitACKCacheProvider;

    @Resource
    private ICache onlineCacheProvider;

    @Override
    public void execute() {
        Date now = new Date();

        Set<Object> keys = waitACKCacheProvider.getKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            int serial = (Integer) iter.next();
            RepeatMSG repeatMSG = (RepeatMSG) waitACKCacheProvider.get(serial);

            if (now.getTime() - repeatMSG.getSendTime().getTime() > 8 * 1000){

                if (!onlineCacheProvider.containsKey(repeatMSG.getTerminal())){

                    waitACKCacheProvider.remove(serial);
                    continue;
                }
                long count = repeatMSG.getCount();
                logger.info("消息重发，终端[{}], 指令[{}], 序列号[{}], 第[{}]次重发...", repeatMSG.getTerminal(), Common.toHex(repeatMSG.getCmd()), serial, count);
                repeatMSG.setSendTime(now);

                MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(repeatMSG.getTerminal());
                pipeline.send(repeatMSG.getTerminal(), repeatMSG.getContent());

                if (count == 3){
                    waitACKCacheProvider.remove(serial);
                }
            }

        }

    }
}
