package com.abqwtb;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class BusFragment extends Fragment implements OnMapReadyCallback {

  private static final String ARG_BUS_ID = "bus_id";

  private int bus_id;

  public BusFragment() {
    // Required empty public constructor
  }

  public static BusFragment newInstance(int bus_id) {
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
      bus_id = getArguments().getInt(ARG_BUS_ID);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_bus, container, false);
    SupportMapFragment mapFragment = SupportMapFragment.newInstance();
    getFragmentManager().beginTransaction().add(R.id.map_container,mapFragment).commit();
    mapFragment.getMapAsync(this);
    return view;
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    LatLng sydney = new LatLng(-33.852, 151.211);
    googleMap.addMarker(new MarkerOptions().position(sydney)
        .title("Marker in Sydney"));
    googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
  }
}
