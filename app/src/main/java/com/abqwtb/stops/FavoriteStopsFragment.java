package com.abqwtb.stops;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.abqwtb.ABQBusApplication;
import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.StopsProvider;
import com.abqwtb.model.BusStop;
import com.abqwtb.schedule.StopFragment;
import com.google.firebase.analytics.FirebaseAnalytics;
//import com.google.android.gms.analytics.HitBuilders;
//import com.google.android.gms.analytics.Tracker;
import java.util.Collections;
import java.util.Set;

public class FavoriteStopsFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor> {

  private StopsAdapter cursorAdapter;
  //private Tracker mTracker;
  private Set<String> savedStops;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    cursorAdapter =
//        new StopsAdapter();

    //getActivity().getSupportLoaderManager().initLoader(1, null, this);

    ABQBusApplication application = (ABQBusApplication) getActivity().getApplication();
//    mTracker = application.getDefaultTracker();
//    mTracker.enableAdvertisingIdCollection(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_stops_list, container, false);

    ListView listContent = (ListView) view.findViewById(R.id.content_list);
    //listContent.setAdapter(cursorAdapter);

    listContent.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
       // StopFragment f = StopFragment.newInstance((BusStop) view.getTag());
//        getFragmentManager().beginTransaction().replace(R.id.main_container, f)
//            .addToBackStack("stop_view").commit();
        //listContent.setVisibility(View.GONE);
        //frameLayout.setVisibility(View.VISIBLE);
      }
    });

    return view;

  }

  @Override
  public void onResume() {
    super.onResume();
    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    savedStops = sharedPref
        .getStringSet(getString(R.string.favorite_stops_key), null);
    if (savedStops != null && savedStops.size() > 0) {
      getActivity().getSupportLoaderManager().restartLoader(1, null, FavoriteStopsFragment.this);
    } else {
      getActivity().getSupportLoaderManager().destroyLoader(1);
    }
//    mTracker.setScreenName("ABQBus Favorite Stops");
//    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    Activity activity = getActivity();
    if (activity != null) {
      FirebaseAnalytics.getInstance(activity).setCurrentScreen(activity, "Favorite Stops", null);
    }
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri uri = StopsProvider.CONTENT_URI;
    return new CursorLoader(getActivity(), uri, new String[]{"stop_code _id", "stop_name",
        "(SELECT group_concat(route)  FROM route_stop_map WHERE stop_code = stops_local.stop_code)",
        "direction"}, "`stop_code` IN (" +
        TextUtils.join(",", Collections.nCopies(savedStops.size(), "?")) + ")",
        savedStops.toArray(new String[savedStops.size()]),
        null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    //cursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    //cursorAdapter.swapCursor(null);
  }

  @Override
  public void onStart() {
    super.onStart();
    ((StopsListActivity) getActivity()).setIsTopLevel(true);
    //((StopsListActivity) getActivity()).setSearchVisible(false);
  }
}
