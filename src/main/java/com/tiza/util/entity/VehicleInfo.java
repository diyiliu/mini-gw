package com.tiza.util.entity;

import com.tiza.util.DateUtil;

import java.util.Date;

/**
 * Description: VehicleInfo
 * Author: DIYILIU
 * Update: 2016-03-25 14:59
 */
public class VehicleInfo {

    private int id;
    private String terminalId;
    private String name;
    private String license;
    private String softVersion;
    private Date createTime;
    private String createTimeStr;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
        setCreateTime(DateUtil.stringToDate(createTimeStr));
    }
}
