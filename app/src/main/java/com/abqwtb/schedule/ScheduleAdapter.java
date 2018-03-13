package com.abqwtb.schedule;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abqwtb.R;
import com.abqwtb.RouteIcon;
import com.abqwtb.model.BusTrip;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalTime;

public class ScheduleAdapter extends ArrayAdapter<BusTrip> {

  private final List<ViewHolder> lstHolders;
  private LayoutInflater lf;
  private Handler mHandler = new Handler();
  private Runnable updateRemainingTimeRunnable = new Runnable() {
    @Override
    public void run() {
      synchronized (lstHolders) {
        LocalTime nowc = LocalTime.now(DateTimeZone.forID("America/Denver"));
        long now = nowc.getMillisOfDay();
        for (ViewHolder holder : lstHolders) {
          holder.updateTime(now);
        }
        //Log.v("Timer","Update Time");
        mHandler.postDelayed(updateRemainingTimeRunnable, 1000);
      }
    }
  };

  public ScheduleAdapter(Context context, BusTrip[] objects) {
    super(context, 0, objects);
    lf = LayoutInflater.from(context);
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
      viewHolder.scheduleTime = convertView.findViewById(R.id.schedule_time);
      viewHolder.scheduleTimer = convertView.findViewById(R.id.schedule_timer);
      viewHolder.delay = convertView.findViewById(R.id.schedule_delay);
      viewHolder.ll = convertView.findViewById(R.id.schedule_routes_layout);
      viewHolder.arrow = convertView.findViewById(R.id.bus_schedule_arrow);
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

    BusTrip trip;

    TextView scheduleTime;
    TextView scheduleTimer;
    TextView delay;
    LinearLayout ll;
    LocalTime expectedTime;
    ImageView arrow;

    public void setData(BusTrip data) {
      trip = data;
      if (trip.scheduledTime != null) {

        scheduleTime.setText(trip.toString());

        expectedTime = trip.scheduledTime.withFieldAdded(DurationFieldType.seconds(),
            Math.round(trip.secondsLate));

        LocalTime nowc = LocalTime.now(DateTimeZone.forID("America/Denver"));
        long now = nowc.getMillisOfDay();
        updateTime(now);

        RouteIcon icon = RouteIcon.routeIcons.get(trip.route);
        ll.removeAllViews();
        if (icon != null) {
          ll.addView(icon.getView(getContext(), ll));
        }

        if (trip.busId > 0) {
          arrow.setVisibility(View.VISIBLE);
        } else {
          arrow.setVisibility(View.INVISIBLE);
        }

        //Log.v("Late", ""+trip.secondsLate);
        if (trip.secondsLate > 0) {
          delay.setTextColor(Color.argb(200, 255, 0, 0));
          delay.setText(String.format("+%.1f", trip.secondsLate / 60));
        } else if (trip.secondsLate < -1) {
          delay.setTextColor(Color.argb(200, 255, 150, 0));
          delay.setText(String.format("-%.1f", Math.abs(trip.secondsLate / 60)));
        } else if (trip.secondsLate == 0) {
          delay.setTextColor(Color.argb(200, 0, 255, 0));
          delay.setText(getContext().getString(R.string.on_time));
        } else {
          delay.setText("");
        }
      } else {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scheduleTimer
            .getLayoutParams();
        params.weight = 10.0f;
        scheduleTimer.setText(getContext().getString(R.string.no_more_busses));
        arrow.setVisibility(View.INVISIBLE);
        scheduleTime.setText("");
        delay.setText("");
      }

    }

    public void updateTime(long now) {
      if (trip.scheduledTime != null) {
        long diff = expectedTime.getMillisOfDay() - now;
        //Log.i("diff",""+scheduled.getTimeInMillis()+ " - " + now + " = " + diff);
        //Compensate for after midnight times
        if (diff < -12 * 60 * 60 * 1000) {
          diff -= 24 * 60 * 60 * 1000;
        }
        if (diff < 0) {
          diff = 0;
        }
        int seconds = (int) (diff / 1000) % 60;
        int minutes = (int) (diff / (1000 * 60));

        scheduleTimer.setText(String.format("%d:%02d", minutes, seconds));
      }
    }
  }
}
