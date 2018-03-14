package com.abqwtb.stops;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.abqwtb.ABQBusApplication;
import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.StopsProvider;
import com.abqwtb.schedule.StopFragment;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import java.util.ArrayList;
import java.util.List;

public class SearchStopsFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor> {

  private StopsAdapter cursorAdapter;
  private Tracker mTracker;
  private int stopId = -1;
  private int routeNum = -1;
  private String stopName = "";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);

    cursorAdapter =
        new StopsAdapter(getActivity(), null, false);

    //getActivity().getSupportLoaderManager().initLoader(2, null, this);

    ABQBusApplication application = (ABQBusApplication) getActivity().getApplication();
    mTracker = application.getDefaultTracker();
    mTracker.enableAdvertisingIdCollection(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_stops_list, container, false);

    ListView listContent = (ListView) view.findViewById(R.id.content_list);
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

    return view;

  }

  @Override
  public void onResume() {
    super.onResume();
    getActivity().getSupportLoaderManager().restartLoader(2, null, SearchStopsFragment.this);
    mTracker.setScreenName("ABQBus Search Stops");
    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = StopsProvider.CONTENT_URI;
    List<String> queryString = new ArrayList<>();
    List<Object> params = new ArrayList<>();
    if (stopId != -1) {
      queryString.add("stop_code = ?");
      params.add(String.valueOf(stopId));
    }
    if (routeNum != -1) {
      queryString.add("',' || routes || ',' LIKE ?");
      params.add("%," + routeNum + ",%");
    }
    if (stopName != null && stopName.length() > 0) {
      queryString.add("stop_name LIKE ?");
      params.add("%" + stopName.replace(' ', '%') + "%");
    }
    return new CursorLoader(getActivity(), uri, new String[]{"stop_code _id", "stop_name",
        "(SELECT group_concat(route)  FROM route_stop_map WHERE stop_code = stops_local.stop_code) routes",
        "direction"}, TextUtils.join(" AND ", queryString),
        params.toArray(new String[params.size()]),
        "stop_lon");

  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
    cursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    cursorAdapter.swapCursor(null);
  }

  @Override
  public void onStart() {
    super.onStart();
    ((StopsListActivity) getActivity()).setIsTopLevel(true);
    //((StopsListActivity) getActivity()).setSearchVisible(true);
  }

  public void onSearch(int stopId, int routeNum, String stopName) {
    this.stopId = stopId;
    this.routeNum = routeNum;
    this.stopName = stopName;
    getActivity().getSupportLoaderManager().restartLoader(2, null, SearchStopsFragment.this);

  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.appbar_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }
}
