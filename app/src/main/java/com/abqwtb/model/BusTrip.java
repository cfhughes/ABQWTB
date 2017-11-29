package com.abqwtb.model;

import java.text.DateFormat;
import java.util.Date;

public class BusTrip {

  public Date scheduledTime;
  public int route;
  public float secondsLate;
  public int busId;

  public BusTrip(Date scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  @Override
  public String toString() {
    return DateFormat.getTimeInstance().format(scheduledTime);
  }
}
