package com.tiza.util.task.impl;

import com.tiza.model.BackupMSG;
import com.tiza.model.SendMSG;
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

    private final static int DELAY = 3;

    @Resource
    private ICache waitACKCacheProvider;

    @Resource
    private ICache matchACKCacheProvider;

    @Resource
    private ICache onlineCacheProvider;

    @Override
    public void execute() {
        Date now = new Date();

        Set<Object> keys = waitACKCacheProvider.getKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            Object key = iter.next();
            BackupMSG backupMSG = (BackupMSG) waitACKCacheProvider.get(key);
            int id = backupMSG.getId();
            int cmd = backupMSG.getCmd();

            if (now.getTime() - backupMSG.getSendTime().getTime() > (backupMSG.getRepeatTime() + DELAY) * 1000) {

                if (!onlineCacheProvider.containsKey(backupMSG.getTerminal())) {
                    logger.warn("终端[{}]离线, 应答命令[{}]超时!", backupMSG.getTerminal(), backupMSG.getCmd());
                    if (id > 0) {
                        toDB(id, cmd);
                    }
                    waitACKCacheProvider.remove(key);
                    continue;
                }

                long count = backupMSG.getCount();
                if (count > backupMSG.getRepeatCount()) {

                    if (id > 0) {
                        toDB(id, cmd);
                    }

                    waitACKCacheProvider.remove(key);
                    continue;
                }
                logger.info("消息重发，终端[{}], 指令[{}], 标识符[{}], 第[{}]次重发...", backupMSG.getTerminal(), CommonUtil.toHex(backupMSG.getCmd()), key, count);
                backupMSG.setSendTime(now);

                MSGSenderTask.send(new SendMSG(backupMSG.getTerminal(), backupMSG.getCmd(), backupMSG.getContent()));
            }
        }

        Set<Object> mkeys = matchACKCacheProvider.getKeys();
        for (Iterator iter = mkeys.iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            BackupMSG backupMSG = (BackupMSG) matchACKCacheProvider.get(key);
            int id = backupMSG.getId();
            int cmd = backupMSG.getCmd();

            if (now.getTime() - backupMSG.getSendTime().getTime() > 30 * 1000) {
                toDB(id, cmd);
                matchACKCacheProvider.remove(key);
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
