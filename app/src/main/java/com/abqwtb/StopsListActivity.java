package com.abqwtb;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.abqwtb.SearchDialog.SearchDialogListener;
import com.abqwtb.stops.FavoriteStopsFragment;
import com.abqwtb.stops.SearchStopsFragment;
import com.abqwtb.stops.StopsListFragment;
import java.io.IOException;

public class StopsListActivity extends AppCompatActivity implements SearchDialogListener,
    OnNavigationItemSelectedListener {

  public static final String STOP_NAME = "STOP_NAME";
  public static final String ROUTE_NUM = "ROUTE_NUM";
  public static final String STOP_ID = "STOP_ID";
  public static final String SEARCH_FRAGMENT_TAG = "SEARCH_FRAGMENT";

  private ActionBar actionbar;
  private DbHelper dbHelper;
  private DrawerLayout mDrawerLayout;
  private boolean topLevel;
  private MenuItem search_menu_item;
  private SearchStopsFragment searchStopsFragment;
  private SearchDialog searchDialog;
  private EditText stopIdSearch;
  private EditText routeNumSearch;
  private EditText stopNameSearch;
  private int stopId = -1;
  private int routeNum = -1;
  private String stopName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

/*    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());*/
    //End Strict mode code
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stops_list);

    mDrawerLayout = findViewById(R.id.drawer_layout);

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    actionbar = getSupportActionBar();
    actionbar.setDisplayHomeAsUpEnabled(true);
    setIsTopLevel(true);

    getDbHelper();
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_container, new StopsListFragment()).commit();
    }

    new LoadIcons().execute();

    if (savedInstanceState != null) {
      stopId = savedInstanceState.getInt(STOP_ID);
      routeNum = savedInstanceState.getInt(ROUTE_NUM);
      stopName = savedInstanceState.getString(STOP_NAME);
    }

    searchDialog = SearchDialog.newInstance(stopId, routeNum, stopName);

  }

  public void setIsTopLevel(boolean topLevel) {
    if (this.topLevel != topLevel) {
      this.topLevel = topLevel;
      if (topLevel) {
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
      } else {
        actionbar.setHomeAsUpIndicator(R.drawable.back_arrow);
      }
    }
  }
/*
  public void setSearchVisible(boolean searchVisible) {
    if (search_menu_item != null){
      search_menu_item.setVisible(searchVisible);
    }
  }*/

  private void dbCreate() {
    dbHelper = new DbHelper(this);
    try {
      dbHelper.createDataBase();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    dbHelper.openDataBase();
  }

  public synchronized DbHelper getDbHelper() {
    if (dbHelper == null) {
      dbCreate();
    }
    return dbHelper;
  }

  @Override
  protected void onDestroy() {
    dbHelper.close();
    super.onDestroy();
  }

/*  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.appbar_menu, menu);
    search_menu_item = menu.findItem(R.id.search);
    //Log.v("Options","Set");
    setSearchVisible(false);
    return true;
  }*/

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (topLevel) {
          mDrawerLayout.openDrawer(GravityCompat.START);
        } else {
          getSupportFragmentManager().popBackStack();
        }
        return true;
      case R.id.search:
        searchDialog.show(getSupportFragmentManager(), "search");
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSearch(DialogFragment dialog) {
    if (searchStopsFragment == null) {
      searchStopsFragment = (SearchStopsFragment) getSupportFragmentManager()
          .findFragmentByTag(SEARCH_FRAGMENT_TAG);
    }
    if (searchStopsFragment != null && searchStopsFragment.isVisible()) {
      stopIdSearch = dialog.getDialog().findViewById(R.id.stop_id_search);
      String stopIdText = stopIdSearch.getText()
          .toString().trim();
      routeNumSearch = dialog.getDialog().findViewById(R.id.route_number_search);
      String routeNumText = routeNumSearch
          .getText().toString().trim();
      stopNameSearch = dialog.getDialog().findViewById(R.id.stop_name_search);
      stopName = stopNameSearch
          .getText().toString().trim();
      try {
        if (stopIdText.isEmpty()) {
          stopId = -1;
        } else {
          stopId = Integer.parseInt(stopIdText);
        }
        if (routeNumText.isEmpty()) {
          routeNum = -1;
        } else {
          routeNum = Integer.parseInt(routeNumText);
        }
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Toast.makeText(this, "Invalid Entry", Toast.LENGTH_SHORT).show();
        return;
      }

      searchStopsFragment.onSearch(stopId, routeNum, stopName);
    }
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.nav_nearest:
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.main_container, new StopsListFragment()).commit();
        break;
      case R.id.nav_favorites:
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.main_container, new FavoriteStopsFragment()).commit();
        break;
      case R.id.nav_search:
        if (searchStopsFragment == null) {
          searchStopsFragment = SearchStopsFragment.newInstance(stopId, routeNum, stopName);
        }
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.main_container, searchStopsFragment, SEARCH_FRAGMENT_TAG).commit();
        break;
      case R.id.nav_feedback:
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
          startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
          startActivity(new Intent(Intent.ACTION_VIEW,
              Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
        break;
    }
    mDrawerLayout.closeDrawers();
    return true;
  }

  public class LoadIcons extends AsyncTask<Object, Object, Object> {

    @Override
    protected Object doInBackground(Object... objects) {
      getDbHelper();

      Cursor routes = dbHelper
          .query("routes", new String[]{"route_short_name", "route_color", "route_text_color"},
              null, null, null, null, null);
      while (routes.moveToNext()) {
        RouteIcon.routeIcons.put(Integer.parseInt(routes.getString(0).trim()),
            new RouteIcon(Integer.parseInt(routes.getString(0).trim()),
                Color.parseColor("#" + routes.getString(1)),
                Color.parseColor("#" + routes.getString(2))));
      }
      routes.close();
      //dbHelper.close();

      /*adapter = new RouteSpinnerAdapter(StopsListActivity.this,
          android.R.layout.simple_spinner_item,
          new ArrayList<RouteIcon>(RouteIcon.routeIcons.values()));
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/

      return null;
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(STOP_ID, stopId);
    outState.putInt(ROUTE_NUM, routeNum);
    outState.putString(STOP_NAME, stopName);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    if (savedInstanceState != null) {
      stopId = savedInstanceState.getInt(STOP_ID);
      routeNum = savedInstanceState.getInt(ROUTE_NUM);
      stopName = savedInstanceState.getString(STOP_NAME);
    }
  }

}
