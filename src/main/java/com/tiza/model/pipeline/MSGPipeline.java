package com.tiza.model.pipeline;

import com.tiza.model.header.Header;

import java.util.Date;

/**
 * Description: MSGPipeline
 * Author: DIYILIU
 * Update: 2016-03-18 16:33
 */
public abstract class MSGPipeline {

    private Date receiveTime;
    private Date sendTime;
    private Header header;

    public abstract void send(String terminal, int cmd, byte[] bytes);

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}
