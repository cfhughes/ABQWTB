package com.abqwtb.stops;

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
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.StopsProvider;
import com.abqwtb.schedule.StopFragment;

import java.util.ArrayList;
import java.util.List;

import static com.abqwtb.StopsListActivity.ROUTE_NUM;
import static com.abqwtb.StopsListActivity.STOP_ID;
import static com.abqwtb.StopsListActivity.STOP_NAME;

public class SearchStopsFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor> {

  private StopsAdapter cursorAdapter;
  //private Tracker mTracker;
  private int stopId = -1;
  private int routeNum = -1;
  private String stopName = "";

  public static SearchStopsFragment newInstance(int stopId, int routeNum, String stopName) {

    Bundle args = new Bundle();

    args.putInt(STOP_ID, stopId);
    args.putInt(ROUTE_NUM, routeNum);
    args.putString(STOP_NAME, stopName);

    SearchStopsFragment fragment = new SearchStopsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);

    cursorAdapter =
        new StopsAdapter(getActivity(), null, false);

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

  @Override
  public void onResume() {
    super.onResume();
    if (getArguments() != null) {
      stopId = getArguments().getInt(STOP_ID);
      routeNum = getArguments().getInt(ROUTE_NUM);
      stopName = getArguments().getString(STOP_NAME);
    }
    getActivity().getSupportLoaderManager().restartLoader(2, null, SearchStopsFragment.this);
//    mTracker.setScreenName("ABQBus Search Stops");
//    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.appbar_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  public void onSearch(int stopId, int routeNum, String stopName) {
    this.stopId = stopId;
    this.routeNum = routeNum;
    this.stopName = stopName;
    Bundle args = getArguments();
    args.putInt(STOP_ID, stopId);
    args.putInt(ROUTE_NUM, routeNum);
    args.putString(STOP_NAME, stopName);
    setArguments(args);
    getActivity().getSupportLoaderManager().restartLoader(2, null, SearchStopsFragment.this);

  }
}
