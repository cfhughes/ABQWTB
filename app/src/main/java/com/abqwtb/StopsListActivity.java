package com.abqwtb;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import java.io.IOException;
import java.util.ArrayList;

public class StopsListActivity extends AppCompatActivity {

  private DbHelper dbHelper;
  private DrawerLayout mDrawerLayout;
  private AutoCompleteTextView routesSpinner;
  private RouteSpinnerAdapter adapter;

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

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionbar = getSupportActionBar();
    actionbar.setDisplayHomeAsUpEnabled(true);
    actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


    getDbHelper();
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_container, new StopsListFragment()).commit();
    }

    new LoadIcons().execute();

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

    MenuItem item = menu.findItem(R.id.spinner);
    routesSpinner = (AutoCompleteTextView) MenuItemCompat.getActionView(item);

    routesSpinner.setAdapter(adapter);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public class LoadIcons extends AsyncTask<Object, Object, Object> {

    @Override
    protected Object doInBackground(Object... objects) {
      getDbHelper();

      Cursor routes = dbHelper.query("routes",new String[]{"route_short_name","route_color","route_text_color"},null,null,null,null,null);
      while (routes.moveToNext()){
        RouteIcon.routeIcons.put(Integer.parseInt(routes.getString(0).trim()),new RouteIcon(Integer.parseInt(routes.getString(0).trim()),
            Color.parseColor("#"+routes.getString(1)),Color.parseColor("#"+routes.getString(2))));
      }
      routes.close();
      //dbHelper.close();

      adapter = new RouteSpinnerAdapter(StopsListActivity.this,
          android.R.layout.simple_spinner_item,
          new ArrayList<RouteIcon>(RouteIcon.routeIcons.values()));
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      return null;
    }
  }
}
