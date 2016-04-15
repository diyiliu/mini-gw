package com.tiza.model;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: BackupMSG
 * Author: DIYILIU
 * Update: 2016-03-22 13:43
 */
public class BackupMSG {

    private int id;
    private int serial;
    private Date sendTime;
    private String terminal;
    private int cmd;

    private int repeatCount;
    private int repeatTime;

    private AtomicLong count = new AtomicLong(0);
    private byte[] content;

    public BackupMSG() {
    }

    public BackupMSG(int serial, Date sendTime, String terminal, int cmd, byte[] content,
                     int repeatCount, int repeatTime) {
        this.serial = serial;
        this.sendTime = sendTime;
        this.terminal = terminal;
        this.cmd = cmd;
        this.repeatCount = repeatCount;
        this.repeatTime = repeatTime;
        this.content = content;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public long getCount() {
        return count.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCount(AtomicLong count) {
        this.count = count;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(int repeatTime) {
        this.repeatTime = repeatTime;
    }
}
