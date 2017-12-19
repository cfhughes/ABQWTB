package com.abqwtb;

import android.Manifest.permission;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.IOException;
import java.net.URI;

public class StopsListActivity extends AppCompatActivity {

  private DbHelper dbHelper;
  private FrameLayout frameLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

/*    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());*/
    //End Strict mode code
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stops_list);
    frameLayout = findViewById(R.id.main_container);

    dbCreate();
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_container, new StopsListFragment()).commit();
    }

  }

  private void dbCreate() {
    dbHelper = new DbHelper(this);
    try {
      dbHelper.createDataBase();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    dbHelper.getReadableDatabase();
    dbHelper.openDataBase();

  }

  public DbHelper getDbHelper() {
    dbCreate();
    return dbHelper;
  }
}
