package com.tiza.util.listener;

import com.tiza.protocol.IDataProcess;
import com.tiza.protocol.m2.M2DataProcess;
import com.tiza.protocol.mobile.MobileDataProcess;
import com.tiza.util.SpringUtil;
import com.tiza.util.cache.ICache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description: CMDInitializer
 * Author: DIYILIU
 * Update: 2016-03-18 9:19
 */
public class CMDInitializer implements ApplicationListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<Class> protocols;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

        logger.info("协议解析初始化...");

        for (Class protocol : protocols) {

            Map parses = SpringUtil.getBeansOfType(protocol);

            for (Iterator iter = parses.keySet().iterator(); iter.hasNext(); ) {
                String key = (String) iter.next();
                IDataProcess process = (IDataProcess) parses.get(key);
                process.init();
            }
        }
    }

    public void setProtocols(List<Class> protocols) {
        this.protocols = protocols;
    }
}
