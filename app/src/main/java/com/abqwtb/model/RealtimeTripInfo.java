package com.abqwtb.model;

import org.joda.time.LocalTime;

public class RealtimeTripInfo implements Comparable<RealtimeTripInfo> {

    private long secondsLate;

    private String tripId;

    private String scheduledTime;

    private String displayTime;

    private String service;

    private String route;

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
}
