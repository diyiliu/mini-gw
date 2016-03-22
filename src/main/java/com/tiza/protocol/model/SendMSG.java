package com.tiza.protocol.model;

/**
 * Description: SendMSG
 * Author: DIYILIU
 * Update: 2016-03-21 13:59
 */
public class SendMSG {

    private String terminalId;
    private int cmd;
    private byte[] content;

    public SendMSG() {

    }

    public SendMSG(String terminalId, int cmd, byte[] content) {
        this.terminalId = terminalId;
        this.cmd = cmd;
        this.content = content;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
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
}
