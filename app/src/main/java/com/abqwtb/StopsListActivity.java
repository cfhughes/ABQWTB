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
import android.view.Menu;
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

  private ActionBar actionbar;
  private DbHelper dbHelper;
  private DrawerLayout mDrawerLayout;
  private boolean topLevel;
  private MenuItem search_menu_item;
  private SearchStopsFragment searchStopsFragment;
  private SearchDialog searchDialog;

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

    searchDialog = new SearchDialog();

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

  public void setSearchVisible(boolean searchVisible) {
    search_menu_item.setVisible(searchVisible);
  }

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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.appbar_menu, menu);
    search_menu_item = menu.findItem(R.id.search);
    setSearchVisible(false);
    return true;
  }

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
    if (searchStopsFragment != null && searchStopsFragment.isVisible()) {
      String stopId = ((EditText) dialog.getDialog().findViewById(R.id.stop_id_search)).getText()
          .toString().trim();
      String routeNum = ((EditText) dialog.getDialog().findViewById(R.id.route_number_search))
          .getText().toString().trim();
      String stopName = ((EditText) dialog.getDialog().findViewById(R.id.stop_name_search))
          .getText().toString().trim();
      int stopIdInt;
      int routeNumInt;
      try {
        if (stopId.isEmpty()) {
          stopIdInt = -1;
        } else {
          stopIdInt = Integer.parseInt(stopId);
        }
        if (routeNum.isEmpty()) {
          routeNumInt = -1;
        } else {
          routeNumInt = Integer.parseInt(routeNum);
        }
      } catch (NumberFormatException e) {
        e.printStackTrace();
        Toast.makeText(this, "Invalid Entry", Toast.LENGTH_SHORT).show();
        return;
      }
      if (searchStopsFragment != null && searchStopsFragment.isVisible()) {
        searchStopsFragment.onSearch(stopIdInt, routeNumInt, stopName);
      }
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
        searchStopsFragment = new SearchStopsFragment();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.main_container, searchStopsFragment).commit();
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
}
