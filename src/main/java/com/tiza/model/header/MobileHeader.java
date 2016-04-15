package com.tiza.model.header;

import com.tiza.model.Tlv;

import java.util.List;

/**
 * Description: MobileHeader
 * Author: DIYILIU
 * Update: 2016-04-13 15:09
 */
public class MobileHeader extends Header {

    private int cmd;
    private String devIMEI;
    private int length;
    private List<Tlv> content = null;

    public MobileHeader() {
    }

    public MobileHeader(String devIMEI) {
        this.devIMEI = devIMEI;
    }

    public MobileHeader(int cmd, String devIMEI, int length) {
        this.cmd = cmd;
        this.devIMEI = devIMEI;
        this.length = length;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getDevIMEI() {
        return devIMEI;
    }

    public void setDevIMEI(String devIMEI) {
        this.devIMEI = devIMEI;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<Tlv> getContent() {
        return content;
    }

    public void setContent(List<Tlv> content) {
        this.content = content;
    }
}
