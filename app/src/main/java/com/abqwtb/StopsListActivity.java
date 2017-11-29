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
import android.widget.ListView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.IOException;
import java.net.URI;

public class StopsListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  private DbHelper dbHelper;
  private FusedLocationProviderClient mFusedLocationClient;
  private LocationCallback mLocationCallback;
  private LocationRequest mLocationRequest;
  private StopsAdapter cursorAdapter;
  private static Location lastLocation = new Location("nothing");

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
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
        .build());
    //End Strict mode code
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stops_list);
    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    mLocationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        lastLocation = locationResult.getLastLocation();
        getSupportLoaderManager().restartLoader(0, null, StopsListActivity.this);
      }
    };

    cursorAdapter =
        new StopsAdapter(StopsListActivity.this, null, false);
    ListView listContent = (ListView) findViewById(R.id.content_list);
    listContent.setAdapter(cursorAdapter);

    mFusedLocationClient.getLastLocation()
        .addOnSuccessListener(StopsListActivity.this, new OnSuccessListener<Location>() {
          @Override
          public void onSuccess(Location location) {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
              lastLocation = location;
            }
          }
        });

    dbCreate();

    new LoadIcons().execute();

    getSupportLoaderManager().initLoader(0, null, StopsListActivity.this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    startLocationUpdates();
  }

  private void startLocationUpdates() {
    createLocationRequest();
    if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      Log.v("Location","Permission not granted");
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }
    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
        mLocationCallback,
        null /* Looper */);
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopLocationUpdates();
  }

  private void stopLocationUpdates() {
    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
  }

  protected void createLocationRequest() {
    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(10000);
    mLocationRequest.setFastestInterval(5000);
    //mLocationRequest.setSmallestDisplacement(20);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = StopsProvider.CONTENT_URI;
    return new CursorLoader(this, uri, new String[]{"stop_code _id","stop_name","(SELECT group_concat(route)  FROM route_stop_map WHERE stop_code = stops_local.stop_code)"},"`stop_lat` > ? AND `stop_lat` < ? AND `stop_lon` > ? AND `stop_lon` < ?", new String[]{
        String.valueOf(lastLocation.getLatitude()-0.015),
        String.valueOf(lastLocation.getLatitude()+0.015),
        String.valueOf(lastLocation.getLongitude()-0.015),
        String.valueOf(lastLocation.getLongitude()+0.015)},
        "((`stop_lat` - "+lastLocation.getLatitude()+") * (`stop_lat` - "+lastLocation.getLatitude()+") + (`stop_lon` - "+lastLocation.getLongitude()+") * (`stop_lon` - "+lastLocation.getLongitude()+"))");
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    cursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    cursorAdapter.swapCursor(null);
  }

  private void dbCreate(){
    dbHelper = new DbHelper(this);
    try {
      dbHelper.createDataBase();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    dbHelper.getReadableDatabase();
    dbHelper.openDataBase();

  }

  public class LoadIcons extends AsyncTask<Object, Object, Object> {

    @Override
    protected Object doInBackground(Object... objects) {

      Cursor routes = dbHelper.query("routes",new String[]{"route_short_name","route_color","route_text_color"},null,null,null,null,null);
      while (routes.moveToNext()){
        RouteIcon.routeIcons.put(Integer.parseInt(routes.getString(0).trim()),new RouteIcon(Integer.parseInt(routes.getString(0).trim()),
            Color.parseColor("#"+routes.getString(1)),Color.parseColor("#"+routes.getString(2))));
      }
      routes.close();
      dbHelper.close();

      return null;
    }
  }

}
