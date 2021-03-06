package com.abqwtb.stops;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abqwtb.R;
import com.abqwtb.model.BusStop;

import java.util.ArrayList;
import java.util.List;

public class StopsAdapter extends RecyclerView.Adapter<StopsAdapter.StopHolder> {
  private List<BusStop> stops = new ArrayList<>();
  private OnStopClickListener stopClickListener;

  public StopsAdapter(OnStopClickListener stopClickListener) {
    this.stopClickListener = stopClickListener;
  }

  @NonNull
  @Override
  public StopHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View stopView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.stop_list_item, parent, false);

    return new StopHolder(stopView);
  }

  @Override
  public void onBindViewHolder(@NonNull StopHolder holder, int position) {
    BusStop stop = stops.get(position);

    holder.setOnClickListener(view -> stopClickListener.onStopClick(stop));

    holder.stopView.setTag(stop);

    holder.stopName.setText(stop.getTitle());

    holder.stopTrips.removeAllViews();

    for (int i = 0; i < stop.getTrips().size(); i++) {
      TextView tripView = new TextView(holder.stopTrips.getContext());
      BusStop.TripHeadSign tripHeadSign = stop.getTrips().get(i);
      tripView.setText(String.format("%s %s", tripHeadSign.getName(), tripHeadSign.getRoute()));
      tripView.setTextColor(Color.parseColor("#"+tripHeadSign.getTextColor()));
      tripView.setBackgroundColor(Color.parseColor("#"+tripHeadSign.getColor()));
      holder.stopTrips.addView(tripView);
    }
  }

  @Override
  public int getItemCount() {
    return stops.size();
  }

  public void setStops(List<BusStop> stops) {
    this.stops = stops;
    notifyDataSetChanged();
  }


//  @NonNull
//  @Override
//  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//    View v = convertView;
//
//    if (v == null) {
//      LayoutInflater vi;
//      vi = LayoutInflater.from(context);
//      v = vi.inflate(resourceLayout, parent, false);
//    }
//
//    BusStop stop = getItem(position);
//
//    v.setTag(stop);
//    TextView name = v.findViewById(R.id.stop_name);
//    name.setText(stop.getTitle());
//
//    LinearLayout trips = v.findViewById(R.id.stop_trips);
//
//    trips.removeAllViews();
//
//    for (int i = 0; i < stop.getTrips().size(); i++) {
//      TextView tripView = new TextView(trips.getContext());
//      BusStop.TripHeadSign tripHeadSign = stop.getTrips().get(i);
//      tripView.setText(String.format("%s %s", tripHeadSign.getName(), tripHeadSign.getRoute()));
//      tripView.setTextColor(Color.parseColor("#"+tripHeadSign.getTextColor()));
//      tripView.setBackgroundColor(Color.parseColor("#"+tripHeadSign.getColor()));
//      trips.addView(tripView);
//    }
//
//    //StopClockView clock = v.findViewById(R.id.stop_clock);
//
//    //clock.setTrips(stop.getTrips());
//
//
//    //direction.setText(cursor.getString(3));
//    //String[] routes = new String[0];
//    //if (cursor.getString(2) != null) {
//    //  routes = cursor.getString(2).split(",");
//    //}
//    //LinearLayout ll = view.findViewById(R.id.routes_layout);
//    //ll.removeAllViews();
//    //for (String route : routes) {
//    //  RouteIcon icon = RouteIcon.routeIcons.get(Integer.parseInt(route.trim()));
//    //  if (icon != null) {
//    //    ll.addView(icon.getView(context, ll));
//    //  }
//    //}
//    return v;
//  }

  static class StopHolder extends RecyclerView.ViewHolder {
    private TextView stopName;
    private LinearLayout stopTrips;
    private View stopView;

    public StopHolder(View stopView) {
      super(stopView);

      this.stopView = stopView;
      stopName = stopView.findViewById(R.id.stop_name);
      stopTrips = stopView.findViewById(R.id.stop_trips);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
      stopView.setOnClickListener(onClickListener);
    }
  }

  public interface OnStopClickListener {
    void onStopClick(BusStop stop);
  }
}
