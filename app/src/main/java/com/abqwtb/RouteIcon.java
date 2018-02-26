package com.abqwtb;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

public class RouteIcon {

  public static Map<Integer, RouteIcon> routeIcons = new HashMap<Integer, RouteIcon>();

  private int stopColor;
  private int textColor;
  private int route;

  public RouteIcon(int route, int stopColor, int textColor) {
    this.route = route;
    this.stopColor = stopColor;
    this.textColor = textColor;
  }

  public View getView(Context context, ViewGroup parent) {

    LayoutInflater inflater = LayoutInflater.from(context);
    TextView icon = (TextView) inflater.inflate(R.layout.route_icon, parent, false);
    icon.setText(String.valueOf(route));
    icon.setTextColor(textColor);
    GradientDrawable gd = (GradientDrawable) icon.getBackground().getCurrent();
    gd.setColor(stopColor);


    return icon;
  }

  public int getStopColor() {
    return stopColor;
  }

  public int getTextColor() {
    return textColor;
  }

  public int getRoute() {
    return route;
  }

  @Override
  public String toString() {
    return String.valueOf(route);
  }
}
