package com.abqwtb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class RouteSpinnerAdapter extends ArrayAdapter<RouteIcon> {

  List<RouteIcon> filteredRoutes = new ArrayList<>();
  private List<RouteIcon> routes;

  public RouteSpinnerAdapter(Context context, int resource, ArrayList<RouteIcon> routes) {
    super(context, resource, routes);
    this.routes = routes;
  }

  @Override
  public int getCount() {
    return filteredRoutes.size();
  }

  @Override
  public Filter getFilter() {
    return new RouteFilter(this, routes);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    RouteIcon routeIcon = filteredRoutes.get(position);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext())
          .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
    }
    TextView text = convertView.findViewById(android.R.id.text1);

    text.setText("" + routeIcon.getRoute());
    text.setBackgroundColor(routeIcon.getStopColor());
    text.setTextColor(routeIcon.getTextColor());

    return convertView;
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    return getView(position, convertView, parent);
  }
}
