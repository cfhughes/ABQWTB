package com.abqwtb.model;

import android.bluetooth.BluetoothClass;

import java.io.Serializable;
import java.util.List;

public class BusStop implements Serializable {

    private String id;

    private String agency;

    private String title;

    private Point location;

    private List<TripHeadSign> trips;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<TripHeadSign> getTrips() {
        return trips;
    }

    public void setTrips(List<TripHeadSign> trips) {
        this.trips = trips;
    }

    public static class Point implements Serializable{
        double x;
        double y;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public static class TripHeadSign implements Serializable {

        private String name;

        private String route;

        private String color;

        private String textColor;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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
    }
}
