package com.abqwtb;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import java.io.IOException;

public class StopsListActivity extends AppCompatActivity {

  private DbHelper dbHelper;
  private DrawerLayout mDrawerLayout;

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

      return null;
    }
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
}
