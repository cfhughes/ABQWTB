package com.abqwtb;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import java.util.List;

public class StopsAdapter extends CursorAdapter {

  private final Context context;
  private LayoutInflater cursorInflater;

  public StopsAdapter(Context context, Cursor c, boolean autoRequery) {
    super(context, c, autoRequery);
    this.context = context;
    cursorInflater = LayoutInflater.from(context);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return cursorInflater.inflate(R.layout.stop_list_item, parent, false);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    TextView name = view.findViewById(R.id.stop_name);
    name.setText(cursor.getString(1));
    String[] routes = new String[0];
    if (cursor.getString(2) != null) {
      routes = cursor.getString(2).split(",");
    }
    LinearLayout ll = view.findViewById(R.id.routes_layout);
    ll.removeAllViews();
    for (String route:routes){
      RouteIcon icon = RouteIcon.routeIcons.get(Integer.parseInt(route.trim()));
      if (icon != null) {
        ll.addView(icon.getView(context, ll));
      }
    }
  }
}
