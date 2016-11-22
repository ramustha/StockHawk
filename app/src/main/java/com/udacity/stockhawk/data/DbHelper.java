package com.udacity.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.udacity.stockhawk.data.Contract.Quote;

public class DbHelper extends SQLiteOpenHelper
{

  static final String NAME = "StockHawk.db";
  private static final int VERSION = 1;

  public DbHelper(Context context)
  {
    super(context, NAME, null, VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {

    String quote = "CREATE TABLE " + Quote.TABLE_NAME + " (" +
        Quote._ID + " INTEGER PRIMARY KEY, " +
        Quote.COLUMN_SYMBOL + " TEXT NOT NULL, " +
        Quote.COLUMN_PRICE + " REAL NOT NULL, " +
        Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, " +
        Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, " +
        "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

    String history = "CREATE TABLE " + Contract.History.TABLE_NAME + " (" +
        Contract.History._ID + " INTEGER PRIMARY KEY, " +
        Contract.History.COLUMN_QUOTE_ID + " INTEGER NOT NULL, " +
        Contract.History.COLUMN_DATE + " LONG NOT NULL, " +
        Contract.History.COLUMN_OPEN + " REAL NOT NULL, " +
        Contract.History.COLUMN_CLOSE + " REAL NOT NULL, " +
        Contract.History.COLUMN_LOW + " REAL NOT NULL, " +
        Contract.History.COLUMN_HIGH + " REAL NOT NULL, " +
        Contract.History.COLUMN_ADJ_CLOSE + " REAL NOT NULL, " +
        Contract.History.COLUMN_VOLUME + " REAL NOT NULL, " +

        " FOREIGN KEY (" + Contract.History.COLUMN_QUOTE_ID + ") REFERENCES " +
        Contract.History.TABLE_NAME + " (" + Quote._ID + ")); ";

    db.execSQL(quote);
    db.execSQL(history);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {

    db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME);
    db.execSQL(" DROP TABLE IF EXISTS " + Contract.History.TABLE_NAME);

    onCreate(db);
  }
}
