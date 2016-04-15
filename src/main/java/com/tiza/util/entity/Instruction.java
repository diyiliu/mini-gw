package com.tiza.util.entity;

/**
 * Description: Instruction
 * Author: DIYILIU
 * Update: 2016-04-15 10:05
 */
public class Instruction {

    private int id;
    private int paramId;
    private String paramValue;

    public Instruction() {
    }

    public Instruction(int id, int paramId, String paramValue) {
        this.id = id;
        this.paramId = paramId;
        this.paramValue = paramValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParamId() {
        return paramId;
    }

    public void setParamId(int paramId) {
        this.paramId = paramId;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
