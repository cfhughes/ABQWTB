package com.abqwtb.stops;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.StopsProvider;
import com.abqwtb.model.BusStop;
import com.abqwtb.schedule.StopFragment;
import com.abqwtb.viewmodel.StopsViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import static com.abqwtb.StopsListActivity.ROUTE_NUM;
import static com.abqwtb.StopsListActivity.STOP_ID;
import static com.abqwtb.StopsListActivity.STOP_NAME;

public class SearchStopsFragment extends Fragment implements StopsAdapter.OnStopClickListener {

  private StopsAdapter stopsAdapter;
  //private Tracker mTracker;
  private String stopId = "";
  private String routeNum = "";
  private String stopName = "";
  private StopsViewModel viewModel;
  private RecyclerView listContent;

  public static SearchStopsFragment newInstance(String stopId, String routeNum, String stopName) {

    Bundle args = new Bundle();

    args.putString(STOP_ID, stopId);
    args.putString(ROUTE_NUM, routeNum);
    args.putString(STOP_NAME, stopName);

    SearchStopsFragment fragment = new SearchStopsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);

    stopsAdapter = new StopsAdapter(this);

    viewModel = new ViewModelProvider(getActivity()).get(StopsViewModel.class);
    viewModel.getSearchStops().observe(this, new Observer<List<BusStop>>() {
      @Override
      public void onChanged(List<BusStop> busStops) {
        if (busStops != null){
          stopsAdapter.setStops(busStops);
        }
      }
    });

//    cursorAdapter =
//        new StopsAdapter();

    //getActivity().getSupportLoaderManager().initLoader(2, null, this);

//    ABQBusApplication application = (ABQBusApplication) getActivity().getApplication();
//    mTracker = application.getDefaultTracker();
//    mTracker.enableAdvertisingIdCollection(true);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_stops_list, container, false);
    
    listContent = (RecyclerView) view.findViewById(R.id.content_list);
    listContent.setLayoutManager(new LinearLayoutManager(getContext()));
    listContent.setAdapter(stopsAdapter);
    //listContent.setAdapter(cursorAdapter);

    return view;

  }

//  @NonNull
//  @Override
//  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//    Uri uri = StopsProvider.CONTENT_URI;
//    List<String> queryString = new ArrayList<>();
//    List<Object> params = new ArrayList<>();
//    if (stopId != -1) {
//      queryString.add("stop_code = ?");
//      params.add(String.valueOf(stopId));
//    }
//    if (routeNum != -1) {
//      queryString.add("',' || routes || ',' LIKE ?");
//      params.add("%," + routeNum + ",%");
//    }
//    if (stopName != null && stopName.length() > 0) {
//      queryString.add("stop_name LIKE ?");
//      params.add("%" + stopName.replace(' ', '%') + "%");
//    }
//    return new CursorLoader(getActivity(), uri, new String[]{"stop_code _id", "stop_name",
//        "(SELECT group_concat(route)  FROM route_stop_map WHERE stop_code = stops_local.stop_code) routes",
//        "direction"}, TextUtils.join(" AND ", queryString),
//        params.toArray(new String[params.size()]),
//        "stop_lon");
//
//  }

  @Override
  public void onStart() {
    super.onStart();
    ((StopsListActivity) getActivity()).setIsTopLevel(true);
    //((StopsListActivity) getActivity()).setSearchVisible(true);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (getArguments() != null) {
      stopId = getArguments().getString(STOP_ID);
      routeNum = getArguments().getString(ROUTE_NUM);
      stopName = getArguments().getString(STOP_NAME);
    }
//    mTracker.setScreenName("ABQBus Search Stops");
//    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    Activity activity = getActivity();
    if (activity != null) {
      FirebaseAnalytics.getInstance(activity).setCurrentScreen(activity, "Search Stops", null);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.appbar_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  public void onSearch(String stopId, String routeNum, String stopName) {
    this.stopId = stopId;
    this.routeNum = routeNum;
    this.stopName = stopName;
    Bundle args = getArguments();
    args.putString(STOP_ID, stopId);
    args.putString(ROUTE_NUM, routeNum);
    args.putString(STOP_NAME, stopName);
    setArguments(args);
    
    runSearch();
    
  }

  private void runSearch() {
    viewModel.searchStops(stopName, routeNum, stopId);
  }

  @Override
  public void onStopClick(BusStop stop) {
    viewModel.setSelectedStop(stop);
    viewModel.clearRealTimeData();
    StopFragment f = StopFragment.newInstance();
    getParentFragmentManager().beginTransaction().replace(R.id.main_container, f, "stop_view")
            .addToBackStack("stop_view").commit();
  }
}
