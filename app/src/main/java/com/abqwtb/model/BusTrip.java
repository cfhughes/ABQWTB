package com.abqwtb.model;

import java.text.DateFormat;
import java.util.Date;

public class BusTrip {

  public long scheduledTime = -1;
  public int route;
  public float secondsLate;
  public int busId;

  @Override
  public String toString() {
    return DateFormat.getTimeInstance().format(scheduledTime);
  }
}
