package com.abqwtb;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import net.danlew.android.joda.JodaTimeAndroid;

public class ABQBusApplication extends Application {
  public static final String CHANNEL_ID = "atmystop_transit";

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
    
    createNotificationChannel();
  }

  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.transit);
      String description = getString(R.string.channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);
      // Register the channel with the system; you can't change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
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
