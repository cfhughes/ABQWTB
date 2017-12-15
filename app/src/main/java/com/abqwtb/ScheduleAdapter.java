package com.abqwtb;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.abqwtb.model.BusTrip;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleAdapter extends ArrayAdapter<BusTrip> {

  private final List<ViewHolder> lstHolders;
  private LayoutInflater lf;
  private Handler mHandler = new Handler();
  private Runnable updateRemainingTimeRunnable = new Runnable() {
    @Override
    public void run() {
      synchronized (lstHolders) {
        long currentTime = System.currentTimeMillis();
        for (ViewHolder holder : lstHolders) {
          holder.updateTime(currentTime);
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
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_list_item, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.scheduleTime = convertView.findViewById(R.id.schedule_time);
      viewHolder.scheduleTimer = convertView.findViewById(R.id.schedule_timer);
      viewHolder.delay = convertView.findViewById(R.id.schedule_delay);
      viewHolder.ll = convertView.findViewById(R.id.schedule_routes_layout);
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

  public class ViewHolder{
    BusTrip trip;

    TextView scheduleTime;
    TextView scheduleTimer;
    TextView delay;
    LinearLayout ll;
    Calendar scheduled;

    public void setData(BusTrip data){
      trip = data;
      if (trip != null) {
        scheduled = Calendar.getInstance();
        scheduled.setTime(trip.scheduledTime);
        scheduled.add(Calendar.SECOND, Math.round(trip.secondsLate));

        updateTime(System.currentTimeMillis());

        scheduleTime.setText(DateFormat.getTimeInstance().format(scheduled.getTime()));

        RouteIcon icon = RouteIcon.routeIcons.get(trip.route);
        ll.removeAllViews();
        ll.addView(icon.getView(getContext(), ll));

        //Log.v("Late", ""+trip.secondsLate);
        if (trip.secondsLate > 0){
          delay.setTextColor(Color.argb(200,255,0,0));
          delay.setText(String.format("+%.1f", trip.secondsLate / 60));
        }else if(trip.secondsLate < -1){
          delay.setTextColor(Color.argb(200,255,150,0));
          delay.setText(String.format("-%.1f", Math.abs(trip.secondsLate / 60)));
        }else if(trip.secondsLate == 0) {
          delay.setTextColor(Color.argb(200,0,255,0));
          delay.setText("On Time");
        }else{
          delay.setText("");
        }
      }else{
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scheduleTimer.getLayoutParams();
        params.weight = 10.0f;
        scheduleTimer.setText(getContext().getString(R.string.no_more_busses));
      }

    }

    public void updateTime(long time){
      if (trip != null) {
        long now = time % (24 * 60 * 60 * 1000);
        long diff = scheduled.getTimeInMillis() - now;
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
