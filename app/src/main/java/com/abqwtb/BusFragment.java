package com.abqwtb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class BusFragment extends Fragment implements OnMapReadyCallback {

  private static final String ARG_BUS_ID = "bus_id";

  private Marker marker;
  private GoogleMap map;
  private Runnable mRunnable;
  private Handler mHandler;
  private RequestQueue queue;

  private StringRequest request;
  private TextView nextStop;
  private ProgressBar progressBar;
  private ProgressBarAnimation progressBarAnimation;
  private Tracker mTracker;

  public BusFragment() {
    // Required empty public constructor
  }

  static BusFragment newInstance(int bus_id) {
    BusFragment fragment = new BusFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_BUS_ID, bus_id);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      int bus_id = getArguments().getInt(ARG_BUS_ID);
      String url = "http://www.abqwtb.com/android_bus.php?bus_id=" + bus_id;
      request = new StringRequest(Method.GET, url, new Listener<String>() {
        @Override
        public void onResponse(String response) {
          //Log.v("Location Response", response);
          String[] coords = response.split(":");
          nextStop.setText(String.format("Next Stop: %s", coords[0]));
          LatLng position = new LatLng(Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
          CameraUpdate update = CameraUpdateFactory.newLatLng(position);
          marker.setPosition(position);
          map.animateCamera(update);
        }
      }, new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
      });
    }
    mHandler = new Handler();
    queue = Volley.newRequestQueue(getContext());

    ABQBusApplication application = (ABQBusApplication) getActivity().getApplication();
    mTracker = application.getDefaultTracker();
    mTracker.enableAdvertisingIdCollection(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_bus, container, false);
    SupportMapFragment mapFragment = SupportMapFragment.newInstance();
    getFragmentManager().beginTransaction().add(R.id.map_container,mapFragment).commit();
    mapFragment.getMapAsync(this);
    nextStop = (TextView) view.findViewById(R.id.bus_next_stop);
    progressBar = (ProgressBar) view.findViewById(R.id.bus_update_progress);
    return view;
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    map = googleMap;
    LatLng abq = new LatLng(35.088208, -106.649647);
    Bitmap orig = BitmapFactory.decodeResource(getResources(),R.drawable.icon);
    Bitmap scaled = Bitmap.createScaledBitmap(orig,128,128,false);
    marker = googleMap.addMarker(new MarkerOptions().position(abq).icon(BitmapDescriptorFactory.fromBitmap(scaled))
        .title("Marker in Sydney"));
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(abq, 14));
    googleMap.setMyLocationEnabled(true);
    queue.add(request);
    progressBarAnimation = new ProgressBarAnimation(progressBar,0,100);
    progressBarAnimation.setDuration(1000*15);
    progressBar.startAnimation(progressBarAnimation);
    mRunnable = new Runnable(){

      @Override
      public void run() {
        queue.add(request);
        mHandler.postDelayed(mRunnable,1000*15); // 15 seconds
        progressBar.startAnimation(progressBarAnimation);
      }
    };
    mHandler.postDelayed(mRunnable,1000*15);
  }

  @Override
  public void onStop() {
    super.onStop();
    mHandler.removeCallbacks(mRunnable);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (map != null) {
      queue.add(request);
      mHandler.postDelayed(mRunnable, 1000 * 15); // 15 seconds
      progressBar.startAnimation(progressBarAnimation);
    }
    mTracker.setScreenName("ABQBus Tracker");
    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private float from;
    private float  to;

    public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
      super();
      this.progressBar = progressBar;
      this.from = from;
      this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
      super.applyTransformation(interpolatedTime, t);
      float value = from + (to - from) * interpolatedTime;
      progressBar.setProgress((int) value);
    }

  }
}
