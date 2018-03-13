package com.abqwtb.model;

import org.joda.time.LocalTime;

public class BusTrip {

  public LocalTime scheduledTime;
  public int route;
  public float secondsLate;
  public int busId;

  @Override
  public String toString() {
    return scheduledTime.toString("h:mm aa");
  }
}
