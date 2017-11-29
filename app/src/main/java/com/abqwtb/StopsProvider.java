package com.abqwtb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import java.io.IOException;

public class StopsProvider extends ContentProvider {

  public static final String PROVIDER_NAME = "com.abqwtb.sqlite";

  public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/stops" );

  private static final UriMatcher uriMatcher ;
  static {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(PROVIDER_NAME, "stops", 1);
  }

  private DbHelper mOpenHelper;

  private SQLiteDatabase db;

  @Override
  public boolean onCreate() {
    mOpenHelper = new DbHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] columns, String selection, String[] args, String sortOrder) {
    db = mOpenHelper.getReadableDatabase();
    Cursor result = null;
    try{
      result = db.query("stops_local",columns,selection,args,null,null,sortOrder);
    }catch(SQLException e){
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    return null;
  }

  @Override
  public int delete(Uri uri, String s, String[] strings) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
    return 0;
  }
}
