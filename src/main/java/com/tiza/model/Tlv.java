package com.tiza.model;

/**
 * Description: Tlv
 * Author: DIYILIU
 * Update: 2016-04-13 15:20
 */
public class Tlv {

    private int tag;
    private int length;
    private byte[] value = null;

    public Tlv() {
    }

    public Tlv(int tag, int length, byte[] value) {
        this.tag = tag;
        this.length = length;
        this.value = value;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
