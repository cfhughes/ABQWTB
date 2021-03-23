package com.abqwtb.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;
import com.abqwtb.stops.StopClockView;
import com.abqwtb.viewmodel.StopsViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_STOP_ID = "stop_id";

  private BusStop stop;
  //private Tracker mTracker;
  private ScheduleAdapter adapter;
  private ListView schedule;
  private Handler mHandler;
  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
  private Runnable mRunnable;
  private Context context;
  private boolean favorite;
  private Set<String> savedStops;
  private StopClockView stopClockView;
  private StopsViewModel viewModel;


  public StopFragment() {
    // Required empty public constructor
  }

  public static StopFragment newInstance() {
    StopFragment fragment = new StopFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(getActivity()).get(StopsViewModel.class);

/*    ABQBusApplication application = (ABQBusApplication) getActivity().getApplication();
    mTracker = application.getDefaultTracker();
    mTracker.enableAdvertisingIdCollection(true);*/

    mHandler = new Handler();
    //queue = Volley.newRequestQueue(getContext());

//    stringRequest = new StringRequest(Request.Method.GET, url,
//        new Response.Listener<String>() {
//          @Override
//          public void onResponse(String response) {
//            if (!isAdded()) {
//              return;
//            }
//            String[] sched = response.split("\\|");
//            BusTrip[] trips = new BusTrip[sched.length];
//
//            for (int i = 0; i < sched.length; i++) {
//              String[] item = sched[i].split(";");
//              try {
//                trips[i] = new BusTrip();
//                DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
//                // testing: item[0] = "25:45:56";
//                int hour = Integer.parseInt(item[0].split(":")[0]);
//                if (hour > 23) {
//                  item[0] = (hour - 24) + item[0].substring(2);
//                }
//                trips[i].setScheduledTime(fmt.parseLocalTime(item[0]));
//                trips[i].setRoute(Integer.parseInt(item[1]));
//                trips[i].setSecondsLate(Float.parseFloat(item[2]));
//                trips[i].setBusId(Integer.parseInt(item[3].trim()));
//              } catch (IllegalArgumentException e) {
//                //Do Nothing, this happens when there is no bus id to parse
//              }
//            }
//            adapter = new ScheduleAdapter(context, trips);
//            schedule.setAdapter(adapter);
//            schedule.setOnItemClickListener(new OnItemClickListener() {
//              @Override
//              public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                if (((ViewHolder) view.getTag()).trip.getBusId() > 0) {
//                  BusFragment f = BusFragment.newInstance(((ViewHolder) view.getTag()).trip.getBusId());
//                  getFragmentManager().beginTransaction().replace(R.id.main_container, f)
//                      .addToBackStack("bus").commit();
//                }
//              }
//            });
//          }
//        }, new Response.ErrorListener() {
//      @Override
//      public void onErrorResponse(VolleyError error) {
//        if (!isAdded()) {
//          return;
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setMessage(
//            "An error occurred while retrieving data, please check your internet connection.");
//        builder.setTitle("Connection Error");
//        builder.setPositiveButton("Ok", new OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialog, int which) {
//
//          }
//        });
//        AlertDialog alert = builder.create();
//        alert.show();
//      }
//    });

  }

  @Override
  public void onStop() {
    super.onStop();
    if (adapter != null) {
      adapter.stopTimer();
    }
    mHandler.removeCallbacks(mRunnable);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_stop, container, false);
    TextView mainText = view.findViewById(R.id.stop_main_text);
//    SQLiteDatabase db = ABQBusApplication.getInstance().getDbHelper().getReadableDatabase();
//    Cursor cursor = db
//        .query("stops_local", new String[]{"stop_name", "direction"}, "stop_code = ?",
//            new String[]{String.valueOf(stop_id)}, null, null, null);
//    cursor.moveToFirst();
//    mainText.setText(cursor.getString(0) + " " + cursor.getString(1));
//    cursor.close();
    //helper.close();

    viewModel.getSelectedStop().observe(getViewLifecycleOwner(), new Observer<BusStop>() {
      @Override
      public void onChanged(BusStop stop) {
        Log.v("Stop Id",stop.getId());
        StopFragment.this.stop = stop;
        mainText.setText(stop.getTitle());
        createStarView(view);
      }
    });

    stopClockView = view.findViewById(R.id.stop_clock_view);
    schedule = view.findViewById(R.id.schedule);

    viewModel.getRealTimeLiveData().observe(getViewLifecycleOwner(), new Observer<List<RealtimeTripInfo>>() {
      @Override
      public void onChanged(List<RealtimeTripInfo> realtimeTripInfos) {
        if (realtimeTripInfos != null) {
          stopClockView.setTrips(realtimeTripInfos);
          Log.v("Trips", String.valueOf(realtimeTripInfos));
          adapter = new ScheduleAdapter(context, realtimeTripInfos, stop);
          schedule.setAdapter(adapter);
        }
      }
    });

    mRunnable = new Runnable() {

      @Override
      public void run() {
        BusStop stop = viewModel.getSelectedStop().getValue();
        viewModel.updateRealTimeData(stop.getAgency(),stop.getId());
        mHandler.postDelayed(mRunnable, 1000 * 15); // 15 seconds
      }
    };
    mHandler.post(mRunnable);

    return view;
  }

  private void createStarView(View view) {
    final ImageView star = view.findViewById(R.id.stop_favorite_star);

    final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

    savedStops = sharedPref
            .getStringSet(getString(R.string.favorite_stops_key), null);
    if (savedStops != null) {
      if (savedStops.contains(String.valueOf(stop.getId()))) {
        favorite = true;
        star.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),
                R.drawable.ic_star));
      }
    }

    star.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (favorite) {
          star.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),
                  R.drawable.ic_star_border));
          SharedPreferences.Editor editor = sharedPref.edit();
          savedStops.remove(String.valueOf(stop.getId()));
          editor.clear();
          editor.putStringSet(getString(R.string.favorite_stops_key), savedStops);
          editor.apply();
          favorite = false;
        } else {
          star.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),
                  R.drawable.ic_star));
          SharedPreferences.Editor editor = sharedPref.edit();
          if (savedStops == null) {
            savedStops = new HashSet<>();
          }
          savedStops.add(String.valueOf(stop.getId()));
          editor.clear();
          editor.putStringSet(getString(R.string.favorite_stops_key), savedStops);
          editor.apply();
          favorite = true;
        }
      }
    });
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    this.context = context;
  }

  @Override
  public void onStart() {
    super.onStart();
    ((StopsListActivity) getActivity()).setIsTopLevel(false);
  }

  @Override
  public void onResume() {
    super.onResume();
    //mTracker.setScreenName("ABQBus Schedule");
    //mTracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(1, "" + stop_id).build());
    Activity activity = getActivity();
    if (activity != null) {
      Bundle bundle = new Bundle();
      bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Schedule");
      bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "StopsListActivity");
      FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }
    if (adapter != null) {
      adapter.startUpdateTimer();
      mHandler.post(mRunnable);
    }
  }
}
