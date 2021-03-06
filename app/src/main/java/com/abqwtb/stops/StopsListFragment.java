package com.abqwtb.stops;


import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.model.BusStop;
import com.abqwtb.schedule.StopFragment;
import com.abqwtb.viewmodel.StopsViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

public class StopsListFragment extends Fragment implements StopsAdapter.OnStopClickListener {

  private static final int PERMISSION_REQUEST_LOCATION = 1;
  private FusedLocationProviderClient mFusedLocationClient;
  private LocationCallback mLocationCallback;
  private LocationRequest mLocationRequest;
  //private Tracker mTracker;
  private RecyclerView listContent;

  private StopsAdapter stopsAdapter;
  private StopsViewModel viewModel;


  public StopsListFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    stopsAdapter = new StopsAdapter(this);

    viewModel = new ViewModelProvider(getActivity()).get(StopsViewModel.class);
    viewModel.getStops().observe(this, new Observer<List<BusStop>>() {
      @Override
      public void onChanged(List<BusStop> busStops) {
        if (busStops != null){
          stopsAdapter.setStops(busStops);
        }
      }
    });

    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    mLocationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        Location location = locationResult.getLastLocation();
        viewModel.setLocation(location.getLatitude(), location.getLongitude());
      }
    };

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_stops_list, container, false);

    listContent = (RecyclerView) view.findViewById(R.id.content_list);
    listContent.setLayoutManager(new LinearLayoutManager(getContext()));
    listContent.setAdapter(stopsAdapter);

    if (ContextCompat.checkSelfPermission(getActivity(),
        Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      mFusedLocationClient.getLastLocation()
          .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
              // Got last known location. In some rare situations this can be null.
              if (location != null) {
                viewModel.setLocation(location.getLatitude(), location.getLongitude());
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
//    mTracker.setScreenName("ABQBus Stops");
//    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    Activity activity = getActivity();
    if (activity != null) {
      Bundle bundle = new Bundle();
      bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Nearest Stops List");
      bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "StopsListActivity");
      FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }
  }

  private void stopLocationUpdates() {
    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
  }

  private void createLocationRequest() {
    mLocationRequest = LocationRequest.create();
    mLocationRequest.setInterval(10000);
    mLocationRequest.setFastestInterval(5000);
    mLocationRequest.setSmallestDisplacement(10);
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
                      viewModel.setLocation(location.getLatitude(), location.getLongitude());
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

  @Override
  public void onStopClick(BusStop stop) {
    viewModel.setSelectedStop(stop);
    viewModel.clearRealTimeData();
    StopFragment f = StopFragment.newInstance();
    getParentFragmentManager().beginTransaction().replace(R.id.main_container, f)
            .addToBackStack("stop_view").commit();
  }
}
