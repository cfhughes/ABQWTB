package com.abqwtb;

import android.database.Cursor;
import android.graphics.Color;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.abqwtb.SearchDialog.SearchDialogListener;
import java.io.IOException;

public class StopsListActivity extends AppCompatActivity implements SearchDialogListener,
    OnNavigationItemSelectedListener {

  private ActionBar actionbar;
  private DbHelper dbHelper;
  private DrawerLayout mDrawerLayout;
  private boolean topLevel;

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
        new SearchDialog().show(getSupportFragmentManager(), "search");
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSearch(DialogFragment dialog) {
    EditText stopId = dialog.getDialog().findViewById(R.id.stop_id_search);
    Log.v("Search", stopId.getText().toString());
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
    }
    mDrawerLayout.closeDrawers();
    return true;
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

      /*adapter = new RouteSpinnerAdapter(StopsListActivity.this,
          android.R.layout.simple_spinner_item,
          new ArrayList<RouteIcon>(RouteIcon.routeIcons.values()));
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/

      return null;
    }
  }
}
