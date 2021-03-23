package com.abqwtb.schedule;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abqwtb.R;
import com.abqwtb.RouteIcon;
import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalTime;

public class ScheduleAdapter extends ArrayAdapter<RealtimeTripInfo> {

  private final List<ViewHolder> lstHolders;
  private LayoutInflater lf;
  private final BusStop stop;
  private Handler mHandler = new Handler();
  private Runnable updateRemainingTimeRunnable = new Runnable() {
    @Override
    public void run() {
      synchronized (lstHolders) {
        LocalTime nowc = LocalTime.now(DateTimeZone.forID("UTC"));
        long now = nowc.getMillisOfDay();
        ListIterator<ViewHolder> iter = lstHolders.listIterator();
        while (iter.hasNext()) {
          if (iter.next().updateTime(now)) {
            iter.remove();
          }
        }
        //Log.v("Timer","Update Time");
        mHandler.postDelayed(updateRemainingTimeRunnable, 1000);
      }
    }
  };

  public ScheduleAdapter(Context context, List<RealtimeTripInfo> objects, BusStop stop) {
    super(context, 0, objects);
    lf = LayoutInflater.from(context);
    this.stop = stop;
    lstHolders = new ArrayList<>();
    startUpdateTimer();
  }

  public void startUpdateTimer() {
    mHandler.postDelayed(updateRemainingTimeRunnable, 1000);
  }


  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = lf.inflate(R.layout.schedule_list_item, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.scheduleTimer = convertView.findViewById(R.id.schedule_timer);
      viewHolder.tripName = convertView.findViewById(R.id.trip_name);
      viewHolder.ll = convertView.findViewById(R.id.schedule_routes_layout);
      //viewHolder.arrow = convertView.findViewById(R.id.bus_schedule_arrow);
      convertView.setTag(viewHolder);
      synchronized (lstHolders) {
        lstHolders.add(viewHolder);
      }
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    viewHolder.setData(getItem(position));

    return convertView;
  }

  public void stopTimer() {
    mHandler.removeCallbacks(updateRemainingTimeRunnable);
  }

  public class ViewHolder {

    RealtimeTripInfo trip;

    TextView tripName;
    TextView scheduleTimer;
    LinearLayout ll;
    LocalTime expectedTime;
    ImageView arrow;

    public void setData(RealtimeTripInfo data) {
      trip = data;
      if (trip.getScheduledTime() != null) {

        expectedTime = LocalTime.parse(trip.getScheduledTime()).withFieldAdded(DurationFieldType.seconds(),
            Math.round(trip.getSecondsLate()));

        LocalTime nowc = LocalTime.now(DateTimeZone.forID("UTC"));
        long now = nowc.getMillisOfDay();
        updateTime(now);

        BusStop.TripHeadSign tripInfo = getInfoForRoute(trip.getRoute());

        ll.removeAllViews();

        if (tripInfo != null) {
          RouteIcon icon = new RouteIcon(tripInfo.getRoute(), Color.parseColor("#" + tripInfo.getColor()), Color.parseColor("#" + tripInfo.getTextColor()));
          tripName.setText(tripInfo.getName());
          if (icon != null) {
            ll.addView(icon.getView(getContext(), ll));
          }
        }
//        if (trip.getBusId() > 0) {
//          arrow.setVisibility(View.VISIBLE);
//        } else {
//          arrow.setVisibility(View.INVISIBLE);
//        }

        //Log.v("Late", ""+trip.secondsLate);
//        if (trip.getSecondsLate() > 0) {
//          delay.setTextColor(Color.argb(200, 255, 0, 0));
//          delay.setText(String.format("+%.1f", (float) trip.getSecondsLate() / 60));
//        } else if (trip.getSecondsLate() < -1) {
//          delay.setTextColor(Color.argb(200, 255, 150, 0));
//          delay.setText(String.format("-%.1f", Math.abs((float) trip.getSecondsLate() / 60)));
//        } else if (trip.getSecondsLate() == 0) {
//          delay.setTextColor(Color.argb(200, 0, 255, 0));
//          delay.setText(getContext().getString(R.string.on_time));
//        } else {
//          delay.setText("");
//        }
      } else {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scheduleTimer
            .getLayoutParams();
        params.weight = 10.0f;
        scheduleTimer.setText(getContext().getString(R.string.no_more_busses));
        arrow.setVisibility(View.INVISIBLE);
        tripName.setText("");
      }

    }

    private BusStop.TripHeadSign getInfoForRoute(String route) {
      for (int i = 0; i < stop.getTrips().size(); i++) {
        BusStop.TripHeadSign current = stop.getTrips().get(i);
        if (current.getRoute().equals(route)) {
          return current;
        }
      }

      return null;
    }

    public boolean updateTime(long now) {
      if (trip.getScheduledTime() != null) {
        long diff = expectedTime.getMillisOfDay() - now;
        //Log.i("diff",""+scheduled.getTimeInMillis()+ " - " + now + " = " + diff);
        //Compensate for after midnight times
        if (diff < -7 * 60 * 60 * 1000) {
          diff += 24 * 60 * 60 * 1000;
        }
        if (diff > 12 * 60 * 60 * 1000) {
          diff -= 24 * 60 * 60 * 1000;
        }
        if (diff < -120 * 1000) {
          Log.v("Removed","Removed "+trip.getDisplayTime());
          remove(trip);
          return true; //remove
        }
        if (diff < 0) {
          diff = 0;
        }
        int seconds = (int) (diff / 1000) % 60;
        int minutes = (int) (diff / (1000 * 60));

        if (minutes < 5) {
          scheduleTimer.setText(String.format("%d:%02d", minutes, seconds));
        } else {
          scheduleTimer.setText(String.format("%d", minutes));
        }
      }
      return false;
    }
  }
}
