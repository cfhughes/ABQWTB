package com.abqwtb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abqwtb.model.BusTrip;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Period;
import java.util.Calendar;

public class ScheduleAdapter extends ArrayAdapter<BusTrip> {

  public ScheduleAdapter(Context context, int resource, BusTrip[] objects) {
    super(context, resource, objects);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_list_item, parent, false);
    }
    BusTrip trip = getItem(position);
    Calendar scheduled = Calendar.getInstance();
    scheduled.setTime(trip.scheduledTime);


    ((TextView)convertView.findViewById(R.id.schedule_time)).setText(DateFormat.getTimeInstance().format(scheduled.getTime()));
    scheduled.add(Calendar.SECOND, Math.round(trip.secondsLate));
    long now = Calendar.getInstance().getTimeInMillis() % (24 * 60 * 60 * 1000);
    long diff = scheduled.getTimeInMillis() - now;
    if (diff < 0){
      diff = 0;
    }
    int seconds = (int) (diff / 1000) % 60;;
    int minutes = (int) (diff / (1000 * 60));
    ((TextView)convertView.findViewById(R.id.schedule_timer)).setText("" + minutes + ":" + seconds);

    LinearLayout ll = convertView.findViewById(R.id.schedule_routes_layout);
    RouteIcon icon = RouteIcon.routeIcons.get(trip.route);
    ll.addView(icon.getView(getContext(),ll));

    convertView.setTag(trip.busId);

    return convertView;
  }
}
