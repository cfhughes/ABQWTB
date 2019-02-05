package com.abqwtb.stops;


import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import com.abqwtb.ABQBusApplication;
import com.abqwtb.DbHelper;
import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.StopsProvider;
import com.abqwtb.schedule.StopFragment;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

public class StopsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final int PERMISSION_REQUEST_LOCATION = 1;
  private static Location lastLocation = new Location("nothing");
  private FusedLocationProviderClient mFusedLocationClient;
  private LocationCallback mLocationCallback;
  private LocationRequest mLocationRequest;
  private StopsAdapter cursorAdapter;
  private DbHelper dbHelper;
  private Tracker mTracker;
  private ListView listContent;


  public StopsListFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    mLocationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        lastLocation = locationResult.getLastLocation();
        getActivity().getSupportLoaderManager().restartLoader(0, null, StopsListFragment.this);
      }
    };

    cursorAdapter =
        new StopsAdapter(getActivity(), null, false);

    getActivity().getSupportLoaderManager().initLoader(0, null, this);

    ABQBusApplication application = (ABQBusApplication) getActivity().getApplication();
    mTracker = application.getDefaultTracker();
    mTracker.enableAdvertisingIdCollection(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_stops_list, container, false);

    listContent = (ListView) view.findViewById(R.id.content_list);
    listContent.setAdapter(cursorAdapter);

    listContent.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        StopFragment f = StopFragment.newInstance((Integer) view.getTag());
        getFragmentManager().beginTransaction().replace(R.id.main_container, f)
            .addToBackStack("stop_view").commit();
        //listContent.setVisibility(View.GONE);
        //frameLayout.setVisibility(View.VISIBLE);
      }
    });

    if (ContextCompat.checkSelfPermission(getActivity(),
        Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      mFusedLocationClient.getLastLocation()
          .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
              // Got last known location. In some rare situations this can be null.
              if (location != null) {
                lastLocation = location;
              }
            }
          });
    } else {
      requestLocationPermission();
    }

    return view;
  }

  @Override
  public void onPause() {
    super.onPause();
    if (ContextCompat.checkSelfPermission(getActivity(),
        Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      stopLocationUpdates();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (ContextCompat.checkSelfPermission(getActivity(),
        Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      startLocationUpdates();
    }
    mTracker.setScreenName("ABQBus Stops");
    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  private void stopLocationUpdates() {
    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
  }

  private void createLocationRequest() {
    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(10000);
    mLocationRequest.setFastestInterval(5000);
    //mLocationRequest.setSmallestDisplacement(20);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  private void startLocationUpdates() {
    createLocationRequest();
    if (ActivityCompat.checkSelfPermission(getContext(), permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(getContext(), permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      Log.v("Location", "Permission not granted");
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
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = StopsProvider.CONTENT_URI;
    return new CursorLoader(getActivity(), uri, new String[]{"stop_code _id", "stop_name",
        "(SELECT group_concat(route)  FROM route_stop_map WHERE stop_code = stops_local.stop_code)",
        "direction"}, "`stop_lat` > ? AND `stop_lat` < ? AND `stop_lon` > ? AND `stop_lon` < ?",
        new String[]{
            String.valueOf(lastLocation.getLatitude() - 0.015),
            String.valueOf(lastLocation.getLatitude() + 0.015),
            String.valueOf(lastLocation.getLongitude() - 0.015),
            String.valueOf(lastLocation.getLongitude() + 0.015)},
        "((`stop_lat` - " + lastLocation.getLatitude() + ") * (`stop_lat` - " + lastLocation
            .getLatitude() + ") + (`stop_lon` - " + lastLocation.getLongitude()
            + ") * (`stop_lon` - " + lastLocation.getLongitude() + "))");
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    cursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    cursorAdapter.swapCursor(null);
  }

  private void requestLocationPermission() {
    // Permission has not been granted and must be requested.
    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
        permission.ACCESS_FINE_LOCATION)) {
      // Provide an additional rationale to the user if the permission was not granted
      // and the user would benefit from additional context for the use of the permission.
      // Display a SnackBar with a button to request the missing permission.
      Snackbar.make(listContent, "Location permission required to determine nearby stops.",
          Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          // Request the permission
          ActivityCompat.requestPermissions(getActivity(),
              new String[]{permission.ACCESS_FINE_LOCATION},
              PERMISSION_REQUEST_LOCATION);
        }
      }).show();

    } else {
      Snackbar.make(listContent,
          "Permission is not available. Requesting location permission.",
          Snackbar.LENGTH_SHORT).show();
      // Request the permission. The result will be received in onRequestPermissionResult().
      ActivityCompat
          .requestPermissions(getActivity(), new String[]{permission.ACCESS_FINE_LOCATION},
              PERMISSION_REQUEST_LOCATION);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_REQUEST_LOCATION: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          // permission was granted, yay! Do the
          // location-related task you need to do.
          if (ContextCompat.checkSelfPermission(getActivity(),
              Manifest.permission.ACCESS_FINE_LOCATION)
              == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                  @Override
                  public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                      lastLocation = location;
                    }
                  }
                });
            startLocationUpdates();
          }
        }
      }
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    ((StopsListActivity) getActivity()).setIsTopLevel(true);
    //((StopsListActivity) getActivity()).setSearchVisible(false);
  }
}
