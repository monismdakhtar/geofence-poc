package com.poc.android.geofencepoc.model.json;


import com.google.gson.annotations.Expose;

import java.util.Date;

@SuppressWarnings("UnusedDeclaration")
public class UpdateDateTimeRequest {
    @Expose
    private long id;
    @Expose
    private Date dateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "UpdateDateTimeRequest{" +
                "id=" + id +
                ", dateTime=" + dateTime +
                '}';
    }
}
