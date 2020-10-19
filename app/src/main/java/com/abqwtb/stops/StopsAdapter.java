package com.abqwtb.stops;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.abqwtb.R;
import com.abqwtb.RouteIcon;
import com.abqwtb.model.BusStop;

import java.util.List;

public class StopsAdapter extends ArrayAdapter<BusStop> {

  private final Context context;
  private final int resourceLayout;

  public StopsAdapter(Context context, int resource, List<BusStop> stops) {
    super(context, resource, stops);
    this.resourceLayout = resource;
    this.context = context;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(context);
      v = vi.inflate(resourceLayout, parent, false);
    }

    BusStop stop = getItem(position);

    v.setTag(stop.getId());
    TextView name = v.findViewById(R.id.stop_name);
    name.setText(stop.getTitle());
    //TextView direction = v.findViewById(R.id.stop_direction);
    //direction.setText(cursor.getString(3));
    //String[] routes = new String[0];
    //if (cursor.getString(2) != null) {
    //  routes = cursor.getString(2).split(",");
    //}
    //LinearLayout ll = view.findViewById(R.id.routes_layout);
    //ll.removeAllViews();
    //for (String route : routes) {
    //  RouteIcon icon = RouteIcon.routeIcons.get(Integer.parseInt(route.trim()));
    //  if (icon != null) {
    //    ll.addView(icon.getView(context, ll));
    //  }
    //}
    return v;
  }


}
