package com.abqwtb;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHelper extends SQLiteOpenHelper {
  String DB_PATH = null;
  private static String DB_NAME = "stops_db";
  private SQLiteDatabase myDataBase;
  private final Context myContext;

  public DbHelper(Context context) {
    super(context, DB_NAME, null, 10);
    this.myContext = context;
    if(android.os.Build.VERSION.SDK_INT >= 17){
      DB_PATH = context.getApplicationInfo().dataDir + "/databases/"+DB_NAME;
    }
    else
    {
      DB_PATH = "/data/data/" + context.getPackageName() + "/databases/"+DB_NAME;
    }
    Log.e("Path 1", DB_PATH);
  }


  public synchronized void createDataBase() throws IOException {
    boolean dbExist = checkDataBase();
    if (dbExist) {
    } else {
      this.getReadableDatabase();
      this.close();
      try {
        copyDataBase();
      } catch (IOException e) {
        throw new Error("Error copying database");
      }
    }
  }

  private boolean checkDataBase() {
    File dbFile = new File(DB_PATH);
    //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
    return dbFile.exists();
  }

  private void copyDataBase() throws IOException {
    InputStream myInput = myContext.getAssets().open(DB_NAME);
    String outFileName = DB_PATH;
    File old = new File(DB_PATH);
    if (!old.delete()){
      Log.i("data", "Error Deleting Old Database");
    }
    OutputStream myOutput = new FileOutputStream(outFileName);
    byte[] buffer = new byte[10];
    int length;
    while ((length = myInput.read(buffer)) > 0) {
      myOutput.write(buffer, 0, length);
    }
    myOutput.flush();
    myOutput.close();
    myInput.close();

  }

  public synchronized void openDataBase() throws SQLException {
    String myPath = DB_PATH;
    myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

  }

  @Override
  public synchronized void close() {
    if (myDataBase != null)
      myDataBase.close();
    super.close();
  }


  @Override
  public void onCreate(SQLiteDatabase db) {
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (newVersion > oldVersion)
      try {
        copyDataBase();
      } catch (IOException e) {
        e.printStackTrace();

      }
  }

  public Cursor query(String table, String[] columns, String selection, String[] selectionArgs,
      String groupBy, String having, String orderBy) {
    return myDataBase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
  }

  public Cursor rawQuery(String query) {
    return myDataBase.rawQuery(query,null);
  }
}