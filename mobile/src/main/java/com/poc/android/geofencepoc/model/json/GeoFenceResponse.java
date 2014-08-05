package com.poc.android.geofencepoc.model.json;

import com.google.gson.annotations.Expose;

import java.util.Date;

@SuppressWarnings("UnusedDeclaration")
public class GeoFenceResponse extends GeoFenceCreateRequest {
    @Expose
    private long id;
    @Expose
    private int radius;
    @Expose
    private Date enterTime;
    @Expose
    private Date dwellTime;
    @Expose
    private Date exitTime;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Date getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(Date enterTime) {
        this.enterTime = enterTime;
    }

    public Date getDwellTime() {
        return dwellTime;
    }

    public void setDwellTime(Date dwellTime) {
        this.dwellTime = dwellTime;
    }

    public Date getExitTime() {
        return exitTime;
    }

    public void setExitTime(Date exitTime) {
        this.exitTime = exitTime;
    }

    @Override
    public String toString() {
        return "GeoFenceResponse{" +
                "id=" + id +
                ", radius=" + radius +
                ", enterTime=" + enterTime +
                ", dwellTime=" + dwellTime +
                ", exitTime=" + exitTime +
                '}';
    }
}
