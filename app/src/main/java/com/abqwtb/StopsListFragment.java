package com.abqwtb;


import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StopsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StopsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  private FusedLocationProviderClient mFusedLocationClient;
  private LocationCallback mLocationCallback;
  private LocationRequest mLocationRequest;
  private StopsAdapter cursorAdapter;
  private static Location lastLocation = new Location("nothing");
  private DbHelper dbHelper;
  private Tracker mTracker;


  public StopsListFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment StopsListFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static StopsListFragment newInstance(String param1, String param2) {
    StopsListFragment fragment = new StopsListFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }

    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    mLocationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        lastLocation = locationResult.getLastLocation();
        getActivity().getSupportLoaderManager().restartLoader(0, null, StopsListFragment.this);
      }
    };

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


    final ListView listContent = (ListView) view.findViewById(R.id.content_list);
    listContent.setAdapter(cursorAdapter);

    listContent.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        StopFragment f = StopFragment.newInstance((Integer) view.getTag());
        getFragmentManager().beginTransaction().replace(R.id.main_container,f).addToBackStack("stop_view").commit();
        //listContent.setVisibility(View.GONE);
        //frameLayout.setVisibility(View.VISIBLE);
      }
    });

    return view;
  }

  @Override
  public void onPause() {
    super.onPause();
    stopLocationUpdates();
  }

  @Override
  public void onResume() {
    super.onResume();
    startLocationUpdates();
    mTracker.setScreenName("ABQBus Stops");
    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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

  private void startLocationUpdates() {
    createLocationRequest();
    if (ActivityCompat.checkSelfPermission(getContext(), permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(getContext(), permission.ACCESS_COARSE_LOCATION)
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
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = StopsProvider.CONTENT_URI;
    return new CursorLoader(getActivity(), uri, new String[]{"stop_code _id","stop_name","(SELECT group_concat(route)  FROM route_stop_map WHERE stop_code = stops_local.stop_code)", "direction"},"`stop_lat` > ? AND `stop_lat` < ? AND `stop_lon` > ? AND `stop_lon` < ?", new String[]{
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


}
