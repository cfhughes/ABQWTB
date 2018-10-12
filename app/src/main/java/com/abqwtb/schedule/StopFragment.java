package com.abqwtb.schedule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.abqwtb.ABQBusApplication;
import com.abqwtb.DbHelper;
import com.abqwtb.R;
import com.abqwtb.StopsListActivity;
import com.abqwtb.bus.BusFragment;
import com.abqwtb.model.BusTrip;
import com.abqwtb.schedule.ScheduleAdapter.ViewHolder;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class StopFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_STOP_ID = "stop_id";

  private int stop_id;
  private Tracker mTracker;
  private ScheduleAdapter adapter;
  private RequestQueue queue;
  private ListView schedule;
  private Handler mHandler;
  private StringRequest stringRequest;
  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
  private Runnable mRunnable;
  private Context context;
  private boolean favorite;
  private Set<String> savedStops;


  public StopFragment() {
    // Required empty public constructor
  }


  public static StopFragment newInstance(int stop_id) {
    StopFragment fragment = new StopFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_STOP_ID, stop_id);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      stop_id = getArguments().getInt(ARG_STOP_ID);
      Log.v("Stop_id", "" + stop_id);
    }

    String url = "http://www.abqwtb.com/android.php?version=6&stop_id=" + stop_id;

    ABQBusApplication application = (ABQBusApplication) getActivity().getApplication();
    mTracker = application.getDefaultTracker();
    mTracker.enableAdvertisingIdCollection(true);

    mHandler = new Handler();
    queue = Volley.newRequestQueue(getContext());

    stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            if (!isAdded()) {
              return;
            }
            String[] sched = response.split("\\|");
            BusTrip[] trips = new BusTrip[sched.length];

            for (int i = 0; i < sched.length; i++) {
              String[] item = sched[i].split(";");
              try {
                trips[i] = new BusTrip();
                DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
                // testing: item[0] = "25:45:56";
                int hour = Integer.parseInt(item[0].split(":")[0]);
                if (hour > 23) {
                  item[0] = (hour - 24) + item[0].substring(2);
                }
                trips[i].scheduledTime = fmt.parseLocalTime(item[0]);
                trips[i].route = Integer.parseInt(item[1]);
                trips[i].secondsLate = Float.parseFloat(item[2]);
                trips[i].busId = Integer.parseInt(item[3].trim());
              } catch (IllegalArgumentException e) {
                //Do Nothing, this happens when there is no bus id to parse
              }
            }
            adapter = new ScheduleAdapter(context, trips);
            schedule.setAdapter(adapter);
            schedule.setOnItemClickListener(new OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (((ViewHolder) view.getTag()).trip.busId > 0) {
                  BusFragment f = BusFragment.newInstance(((ViewHolder) view.getTag()).trip.busId);
                  getFragmentManager().beginTransaction().replace(R.id.main_container, f)
                      .addToBackStack("bus").commit();
                }
              }
            });
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        if (!isAdded()) {
          return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(
            "An error occurred while retrieving data, please check your internet connection.");
        builder.setTitle("Connection Error");
        builder.setPositiveButton("Ok", new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {

          }
        });
        AlertDialog alert = builder.create();
        alert.show();
      }
    });

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
    DbHelper helper = ((StopsListActivity) context).getDbHelper();
    Cursor cursor = helper
        .query("stops_local", new String[]{"stop_name", "direction"}, "stop_code = ?",
            new String[]{String.valueOf(stop_id)}, null, null, null);
    cursor.moveToFirst();
    mainText.setText(cursor.getString(0) + " " + cursor.getString(1));
    cursor.close();
    //helper.close();

    final ImageView star = view.findViewById(R.id.stop_favorite_star);

    final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

    savedStops = sharedPref
        .getStringSet(getString(R.string.favorite_stops_key), null);
    if (savedStops != null) {
      if (savedStops.contains(String.valueOf(stop_id))) {
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
          savedStops.remove(String.valueOf(stop_id));
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
          savedStops.add(String.valueOf(stop_id));
          editor.clear();
          editor.putStringSet(getString(R.string.favorite_stops_key), savedStops);
          editor.apply();
          favorite = true;
        }
      }
    });

    schedule = view.findViewById(R.id.schedule);

    mRunnable = new Runnable() {

      @Override
      public void run() {
        queue.add(stringRequest);
        mHandler.postDelayed(mRunnable, 1000 * 15); // 15 seconds
      }
    };
    mHandler.post(mRunnable);

    return view;
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
    mTracker.setScreenName("ABQBus Schedule");
    mTracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(1, "" + stop_id).build());
    if (adapter != null) {
      adapter.startUpdateTimer();
      mHandler.post(mRunnable);
    }
  }
}
