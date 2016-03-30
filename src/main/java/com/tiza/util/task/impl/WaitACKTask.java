package com.tiza.util.task.impl;

import com.tiza.protocol.model.BackupMSG;
import com.tiza.protocol.model.pipeline.MSGPipeline;
import com.tiza.util.CommonUtil;
import com.tiza.util.cache.ICache;
import com.tiza.util.config.Constant;
import com.tiza.util.task.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

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
            BackupMSG backupMSG = (BackupMSG) waitACKCacheProvider.get(serial);
            int id = backupMSG.getId();
            int cmd = backupMSG.getCmd();

            if (now.getTime() - backupMSG.getSendTime().getTime() > 8 * 1000) {

                if (!onlineCacheProvider.containsKey(backupMSG.getTerminal())) {

                    if (id > 0) {
                        toDB(id, cmd);
                    }
                    waitACKCacheProvider.remove(serial);
                    continue;
                }

                long count = backupMSG.getCount();
                if (count > 2) {

                    if (id > 0) {
                        toDB(id, cmd);
                    }

                    waitACKCacheProvider.remove(serial);
                    continue;
                }

                logger.info("消息重发，终端[{}], 指令[{}], 序列号[{}], 第[{}]次重发...", backupMSG.getTerminal(), CommonUtil.toHex(backupMSG.getCmd()), serial, count);
                backupMSG.setSendTime(now);

                MSGPipeline pipeline = (MSGPipeline) onlineCacheProvider.get(backupMSG.getTerminal());
                pipeline.send(backupMSG.getTerminal(), backupMSG.getCmd(), backupMSG.getContent());

            }
        }
    }

    /**
     * 应答超时
     *
     * @param id
     * @param cmd
     */
    public void toDB(int id, int cmd) {

        Map valueMap = new HashMap() {
            {
                this.put("ResponseStatus", 10);
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
