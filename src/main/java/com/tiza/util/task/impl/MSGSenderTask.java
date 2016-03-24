package com.tiza.util.task.impl;

import com.tiza.protocol.model.SendMSG;
import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.util.Common;
import com.tiza.util.cache.ICache;
import com.tiza.util.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Description: MSGSenderTask
 * Author: DIYILIU
 * Update: 2016-03-21 13:55
 */
public class MSGSenderTask implements ITask {

    private static ConcurrentLinkedQueue<SendMSG> msgPool = new ConcurrentLinkedQueue<SendMSG>();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache onlineCacheProvider;

    @Override
    public void execute() {

        while (!msgPool.isEmpty()){

            SendMSG msg = msgPool.poll();

            String terminalId = msg.getTerminalId();
            int cmd = msg.getCmd();
            byte[] content = msg.getContent();

            if (onlineCacheProvider.containsKey(terminalId)){
                logger.info("下发消息，终端[{}], 命令[{}H], 内容[{}]", terminalId, Common.toHex(cmd), Common.bytesToString(content));

                MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(terminalId);
                pipeline.setSendTime(new Date());
                pipeline.send(terminalId, content);
            }
        }

    }

    public static void send(SendMSG msg){

        msgPool.add(msg);
    }
}
