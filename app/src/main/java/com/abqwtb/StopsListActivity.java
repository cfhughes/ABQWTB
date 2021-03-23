package com.abqwtb;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.abqwtb.SearchDialog.SearchDialogListener;
import com.abqwtb.notifications.NotificationService;
import com.abqwtb.stops.FavoriteStopsFragment;
import com.abqwtb.stops.SearchStopsFragment;
import com.abqwtb.stops.StopsListFragment;
import com.abqwtb.viewmodel.StopsViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import java.io.IOException;

public class StopsListActivity extends AppCompatActivity implements SearchDialogListener,
    OnNavigationItemSelectedListener {

  public static final String STOP_NAME = "STOP_NAME";
  public static final String ROUTE_NUM = "ROUTE_NUM";
  public static final String STOP_ID = "STOP_ID";
  public static final String SEARCH_FRAGMENT_TAG = "SEARCH_FRAGMENT";

  private ActionBar actionbar;
  private DrawerLayout mDrawerLayout;
  private boolean topLevel;
  private MenuItem search_menu_item;
  private SearchStopsFragment searchStopsFragment;
  private SearchDialog searchDialog;
  private EditText stopIdSearch;
  private EditText routeNumSearch;
  private EditText stopNameSearch;
  private String stopId;
  private String routeNum;
  private String stopName;
  private StopsViewModel viewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    viewModel = new ViewModelProvider(this).get(StopsViewModel.class);

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

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_container, new StopsListFragment()).commit();
    }

    //new LoadIcons().execute();

    if (savedInstanceState != null) {
      stopId = savedInstanceState.getString(STOP_ID);
      routeNum = savedInstanceState.getString(ROUTE_NUM);
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

  @Override
  protected void onDestroy() {
    ABQBusApplication.getInstance().getDbHelper().close();
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
  protected void onPause() {
    super.onPause();

    Fragment stopView = getSupportFragmentManager().findFragmentByTag("stop_view");
    if (stopView != null && stopView.isVisible()) {
      Intent intent = new Intent(this, NotificationService.class);
      intent.putExtra("stop",viewModel.getSelectedStop().getValue());
      startService(intent);
    }
  }

  @Override
  public void onSearch(DialogFragment dialog) {
    if (searchStopsFragment == null) {
      searchStopsFragment = (SearchStopsFragment) getSupportFragmentManager()
          .findFragmentByTag(SEARCH_FRAGMENT_TAG);
    }
    if (searchStopsFragment != null && searchStopsFragment.isVisible()) {
      stopIdSearch = dialog.getDialog().findViewById(R.id.stop_id_search);
      String stopId = stopIdSearch.getText()
          .toString().trim();
      routeNumSearch = dialog.getDialog().findViewById(R.id.route_number_search);
      String routeNum = routeNumSearch
          .getText().toString().trim();
      stopNameSearch = dialog.getDialog().findViewById(R.id.stop_name_search);
      stopName = stopNameSearch
          .getText().toString().trim();

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

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(STOP_ID, stopId);
    outState.putString(ROUTE_NUM, routeNum);
    outState.putString(STOP_NAME, stopName);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    if (savedInstanceState != null) {
      stopId = savedInstanceState.getString(STOP_ID);
      routeNum = savedInstanceState.getString(ROUTE_NUM);
      stopName = savedInstanceState.getString(STOP_NAME);
    }
  }

//  public class LoadIcons extends AsyncTask<Object, Object, Object> {
//
//    @Override
//    protected Object doInBackground(Object... objects) {
//      SQLiteDatabase db = ABQBusApplication.getInstance().getDbHelper().getReadableDatabase();
//
//      Cursor routes = db
//          .query("routes", new String[]{"route_short_name", "route_color", "route_text_color"},
//              null, null, null, null, null);
//      while (routes.moveToNext()) {
//        RouteIcon.routeIcons.put(Integer.parseInt(routes.getString(0).trim()),
//            new RouteIcon(Integer.parseInt(routes.getString(0).trim()),
//                Color.parseColor("#" + routes.getString(1).trim()),
//                Color.parseColor("#" + routes.getString(2).trim())));
//      }
//      routes.close();
//      //dbHelper.close();
//
//      /*adapter = new RouteSpinnerAdapter(StopsListActivity.this,
//          android.R.layout.simple_spinner_item,
//          new ArrayList<RouteIcon>(RouteIcon.routeIcons.values()));
//      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
//
//      return null;
//    }
//  }

}
