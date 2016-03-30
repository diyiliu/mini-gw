package com.tiza.util.client.impl;

import com.tiza.util.cache.ICache;
import com.tiza.util.client.IClient;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Scanner;

/**
 * Description: MonitorClient
 * Author: DIYILIU
 * Update: 2016-03-22 10:36
 */
public class MonitorClient extends Thread implements IClient{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICache onlineCacheProvider;

    @Resource
    private ICache monitorCacheProvider;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;

    @Override
    public void init() {
        options = new Options();
        options.addOption("h", "help", false, "help information");
        options.addOption("m", "monitor", true, "监控车辆 格式[-m terminal '开始监控'; -m c '取消监控']");
        parser = new DefaultParser();

        this.start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        for (; ; ) {
            String in = scanner.nextLine();

            try {
                cmd = parser.parse(options, new String[]{in});

                if (cmd.hasOption("h")) {

                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("help", options);
                } else if (cmd.hasOption("m")) {
                    String terminal = cmd.getOptionValue("m").trim();

                    if (terminal.trim().equalsIgnoreCase("C")){
                        System.out.println("取消监控车辆:" +  monitorCacheProvider.getKeys());
                        monitorCacheProvider.clear();
                    }else if (onlineCacheProvider.containsKey(terminal)){
                        System.out.println("开始监控车辆[" + terminal + "]");
                        monitorCacheProvider.put(terminal, new Date());
                    }else {
                        System.out.println("监控失败！车辆离线[" + terminal + "]");
                    }
                } else {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("cmd error", options);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                logger.error("监控车辆异常！");
            }
        }
    }
}
