/*
package com.abqwtb;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHelper extends SQLiteOpenHelper {

  public static final int VERSION = 13; //13 is new as of 11/25/2019
  private static String DB_NAME = "stops_db.sqlite3";
  private final Context myContext;
  String DB_PATH = null;
  private SQLiteDatabase myDataBase;

  public DbHelper(Context context) {
    super(context, DB_NAME, null, VERSION);
    this.myContext = context;
    if (android.os.Build.VERSION.SDK_INT >= 17) {
      DB_PATH = context.getApplicationInfo().dataDir + "/databases/" + DB_NAME;
    } else {
      DB_PATH = "/data/data/" + context.getPackageName() + "/databases/" + DB_NAME;
    }
    Log.i("Path 1", DB_PATH);
  }

  private static DbHelper instance;
  public static synchronized DbHelper getInstance(Context context){
    if (instance == null){
      instance = new DbHelper(context);
    }
    return instance;
  }


  private synchronized void createDataBase() {
      boolean dbExist = checkDataBase();
      if (dbExist) {
        try {
          myDataBase = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
          Cursor c = rawQuery("SELECT * FROM version");
          c.moveToFirst();
          int v = c.getInt(0);
          if (v < VERSION) {
            Log.v("SQLite version", "Upgrading to version "+VERSION);
            copyDataBase();
          }
        } catch (SQLException e) {
          e.printStackTrace();
          copyDataBase();
        } finally {
          //close();
        }
      } else {
        //this.getWritableDatabase();
        //this.close();

        Log.v("No DataBase", "No Database found, copying ");
        copyDataBase();
      }

  }

  private boolean checkDataBase() {
    File dbFile = new File(DB_PATH);
    //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
    return dbFile.exists();
  }

  private void copyDataBase() {
    close();
    Log.i("dbcopy", "Copying Database File");
    try {
      InputStream myInput = myContext.getAssets().open(DB_NAME);
      String outFileName = DB_PATH;
      File old = new File(DB_PATH);
      if (!old.delete()) {
        Log.i("data", "Error Deleting Old Database");
      }

      OutputStream myOutput = new FileOutputStream(outFileName);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = myInput.read(buffer)) > 0) {
        myOutput.write(buffer, 0, length);
      }
      myOutput.flush();
      myOutput.close();
      myInput.close();
    }catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Log.v("done", "finished copying db");
  }

*/
/*  public synchronized void openDataBase() throws SQLException, IOException {
    createDataBase();
    String myPath = DB_PATH;
    if (myDataBase == null || !myDataBase.isOpen()){
      Log.v("OpenDB", "Opening Database");
      myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
  }*//*


  @Override
  public synchronized void close() {
    if (myDataBase != null &&myDataBase.isOpen()) {
      Log.v("ClosingDB","Closing Database");
      myDataBase.close();
      myDataBase = null;
    }
  }


  @Override
  public void onCreate(SQLiteDatabase db) {
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.i("Upgrade", "Upgrading Database " + oldVersion);
    Log.w("Upgrading","Why is it upgrading this way?");
*/
/*    if (newVersion > oldVersion) {
      try {
        copyDataBase();
      } catch (IOException e) {
        e.printStackTrace();

      }
    }*//*

  }

  public Cursor query(String table, String[] columns, String selection, String[] selectionArgs,
      String groupBy, String having, String orderBy) {
    getReadableDatabase();
    return myDataBase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
  }

  public Cursor rawQuery(String query) {
    getReadableDatabase();
    return myDataBase.rawQuery(query, null);
  }

  @Override
  public synchronized SQLiteDatabase getReadableDatabase() {
    if (myDataBase != null && myDataBase.isOpen()) {
      return myDataBase;  // The database is already open for business
    }

    createDataBase();

    myDataBase = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
    return myDataBase;
  }

  @Override
  public synchronized SQLiteDatabase getWritableDatabase() {
    //Database should be readonly
    return null;
  }
}*/
