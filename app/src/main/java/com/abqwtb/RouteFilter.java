package com.abqwtb;

import android.widget.Filter;
import java.util.ArrayList;
import java.util.List;

class RouteFilter extends Filter {

  RouteSpinnerAdapter adapter;
  List<RouteIcon> originalList;
  List<RouteIcon> filteredList;

  public RouteFilter(RouteSpinnerAdapter adapter, List<RouteIcon> originalList) {
    super();
    this.adapter = adapter;
    this.originalList = originalList;
    this.filteredList = new ArrayList<>();
  }

  @Override
  protected FilterResults performFiltering(CharSequence constraint) {
    filteredList.clear();
    final FilterResults results = new FilterResults();

    if (constraint == null || constraint.length() == 0) {
      filteredList.addAll(originalList);
    } else {
      final String filterPattern = constraint.toString().toLowerCase().trim();

      // Your filtering logic goes in here
      for (final RouteIcon route : originalList) {
        if (String.valueOf(route.getRoute()).toLowerCase().contains(filterPattern)) {
          filteredList.add(route);
        }
      }
    }
    results.values = filteredList;
    results.count = filteredList.size();
    return results;
  }

  @Override
  protected void publishResults(CharSequence constraint, FilterResults results) {
    adapter.filteredRoutes.clear();
    adapter.filteredRoutes.addAll((List) results.values);
    adapter.notifyDataSetChanged();
  }

  @Override
  public String convertResultToString(Object resultValue) {
    return String.valueOf(((RouteIcon) resultValue).getRoute());
  }
}
