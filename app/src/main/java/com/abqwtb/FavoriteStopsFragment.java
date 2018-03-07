package com.abqwtb;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import java.util.Collections;
import java.util.Set;

public class FavoriteStopsFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor> {

  private StopsAdapter cursorAdapter;
  private Tracker mTracker;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    cursorAdapter =
        new StopsAdapter(getActivity(), null, false);

    //getActivity().getSupportLoaderManager().initLoader(1, null, this);

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
    getActivity().getSupportLoaderManager().restartLoader(1, null, FavoriteStopsFragment.this);
    mTracker.setScreenName("ABQBus Favorite Stops");
    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = StopsProvider.CONTENT_URI;
    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    Set<String> savedStops = sharedPref
        .getStringSet(getString(R.string.favorite_stops_key), null);
    if (savedStops != null && savedStops.size() > 0) {
      Log.v("STOPS", savedStops.toString());
      return new CursorLoader(getActivity(), uri, new String[]{"stop_code _id", "stop_name",
          "(SELECT group_concat(route)  FROM route_stop_map WHERE stop_code = stops_local.stop_code)",
          "direction"}, "`stop_code` IN (" +
          TextUtils.join(",", Collections.nCopies(savedStops.size(), "?")) + ")",
          savedStops.toArray(new String[savedStops.size()]),
          null);
    } else {
      return null;
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    cursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    cursorAdapter.swapCursor(null);
  }

  @Override
  public void onStart() {
    super.onStart();
    ((StopsListActivity) getActivity()).setIsTopLevel(true);
  }
}
