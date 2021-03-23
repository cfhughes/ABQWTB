package com.abqwtb.model;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

public class RealtimeTripInfo implements Comparable<RealtimeTripInfo> {

    private long secondsLate;

    private String tripId;

    private String scheduledTime;

    private String displayTime;

    private String service;

    private String route;

    private String color;

    private String textColor;

    public long getSecondsLate() {
        return secondsLate;
    }

    public void setSecondsLate(long secondsLate) {
        this.secondsLate = secondsLate;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    @Override
    public int compareTo(RealtimeTripInfo o) {
        return scheduledTime.compareTo(o.getScheduledTime());
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public long secondsFromNow() {
        LocalTime time = LocalTime.parse(getScheduledTime());
        LocalTime now = LocalTime.now(DateTimeZone.forOffsetHours(0));
        long secondsFromNow = (time.getMillisOfDay() - now.getMillisOfDay() + (getSecondsLate() * 1000))/1000;
        if (secondsFromNow < -7 * 60 * 60) {
            secondsFromNow += 24 * 60 * 60;
        }
        else if (secondsFromNow > 12 * 60 * 60) {
            secondsFromNow -= 24 * 60 * 60;
        }
        return secondsFromNow;
    }
}
