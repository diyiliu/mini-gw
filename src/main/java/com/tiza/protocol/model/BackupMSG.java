package com.tiza.protocol.model;

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
    private AtomicLong count = new AtomicLong(0);
    private byte[] content;

    public BackupMSG() {
    }

    public BackupMSG(int serial, Date sendTime, String terminal, int cmd, byte[] content) {
        this.serial = serial;
        this.sendTime = sendTime;
        this.terminal = terminal;
        this.cmd = cmd;
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
}
