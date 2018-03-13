package com.abqwtb;

import android.app.Application;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import net.danlew.android.joda.JodaTimeAndroid;

public class ABQBusApplication extends Application {

  private static GoogleAnalytics sAnalytics;
  private static Tracker sTracker;

  @Override
  public void onCreate() {
    super.onCreate();

    sAnalytics = GoogleAnalytics.getInstance(this);

//    Stetho.initializeWithDefaults(this);
    JodaTimeAndroid.init(this);
  }

  /**
   * Gets the default {@link Tracker} for this {@link Application}.
   * @return tracker
   */
  synchronized public Tracker getDefaultTracker() {
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    if (sTracker == null) {
      sTracker = sAnalytics.newTracker(R.xml.global_tracker);
    }

    return sTracker;
  }

}
