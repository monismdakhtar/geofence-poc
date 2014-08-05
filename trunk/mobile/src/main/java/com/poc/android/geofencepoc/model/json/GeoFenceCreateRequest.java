package com.poc.android.geofencepoc.model.json;

import com.google.gson.annotations.Expose;

import java.util.Date;

@SuppressWarnings("UnusedDeclaration")
public class GeoFenceCreateRequest {
    @Expose
    private String deviceId;
    @Expose
    private double latitude;
    @Expose
    private double longitude;
    @Expose
    private Date createTime;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "GeoFenceUpdateRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", createTime=" + createTime +
                '}';
    }
}
