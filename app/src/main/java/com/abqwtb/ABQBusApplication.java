package com.abqwtb;

import android.app.Application;
import net.danlew.android.joda.JodaTimeAndroid;

public class ABQBusApplication extends Application {

/*  private static GoogleAnalytics sAnalytics;
  private static Tracker sTracker;*/

  private static ABQBusApplication instance;
  private WtbDbHelper dbHelper;

  @Override
  public void onCreate() {
    instance = this;
    super.onCreate();

//    sAnalytics = GoogleAnalytics.getInstance(this);

//    Stetho.initializeWithDefaults(this);
    JodaTimeAndroid.init(this);
  }

  /**
   * Gets the default Tracker for this {@link Application}.
   *
   * @return tracker
   */
/*  synchronized public Tracker getDefaultTracker() {
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    if (sTracker == null) {
      sTracker = sAnalytics.newTracker(R.xml.global_tracker);
    }

    return sTracker;
  }*/

  public static ABQBusApplication getInstance(){
    return instance;
  }

  synchronized public WtbDbHelper getDbHelper(){
    if (dbHelper == null){
      dbHelper = new WtbDbHelper(this);
    }
    return dbHelper;
  }
}
