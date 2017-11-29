package com.abqwtb;

import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.abqwtb.model.BusTrip;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class StopFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_STOP_ID = "stop_id";

  private int stop_id;
  private TextView mainText;

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
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_stop, container, false);
    mainText = view.findViewById(R.id.stop_main_text);
    DbHelper helper = ((StopsListActivity)getActivity()).getDbHelper();
    helper.openDataBase();
    Cursor cursor = helper.query("stops_local",new String[]{"stop_name"},"stop_id = ?",
        new String[]{String.valueOf(stop_id)},null,null,null);
    cursor.moveToFirst();
    mainText.setText(cursor.getString(0));
    cursor.close();
    helper.close();

    final ListView schedule = view.findViewById(R.id.schedule);

    RequestQueue queue = Volley.newRequestQueue(getContext());

    String url ="http://www.abqwtb.com/android.php?version=6&stop_id="+stop_id;

    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
          @Override
          public void onResponse(String response) {
            // Display the first 500 characters of the response string.
            String[] sched = response.split("\\|");
            BusTrip[] trips = new BusTrip[sched.length];
            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            for (int i = 0;i < sched.length;i++){
              String[] item = sched[i].split(";");
              try {
                trips[i] = new BusTrip(sdf.parse(item[0]));
                trips[i].route = Integer.parseInt(item[1]);
                trips[i].secondsLate = Float.parseFloat(item[2]);
                trips[i].busId = Integer.parseInt(item[3].trim());
              } catch (ParseException e) {
                //e.printStackTrace();
              } catch (NumberFormatException e) {
                //e.printStackTrace();
              }
            }
            ArrayAdapter<BusTrip> adapter = new ScheduleAdapter(getContext(),android.R.layout.simple_list_item_1, trips);
            schedule.setAdapter(adapter);
            schedule.setOnItemClickListener(new OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BusFragment f = BusFragment.newInstance((Integer) view.getTag());
                getFragmentManager().beginTransaction().replace(R.id.main_container,f).addToBackStack("bus").commit();
              }
            });
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {

      }
    });
// Add the request to the RequestQueue.
    queue.add(stringRequest);


    return view;
  }
}
