package com.udacity.stockhawk.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.udacity.stockhawk.data.Contract.History.getSymbolFromUri;
import static com.udacity.stockhawk.data.Contract.PATH_HISTORY_WITH_SYMBOL;

public class StockProvider extends ContentProvider
{

  static final int QUOTE = 100;
  static final int QUOTE_FOR_SYMBOL = 101;
  static final int HISTORY = 102;
  static final int HISTORY_FOR_SYMBOL = 103;
  private static final SQLiteQueryBuilder sHistoryByQuoteSettingQueryBuilder;
  //Quote.symbol = ?
  private static final String sQuoteSelection =
      Contract.Quote.TABLE_NAME +
          "." + Contract.Quote.COLUMN_SYMBOL + " = ? ";
  static UriMatcher uriMatcher = buildUriMatcher();

  static {
    sHistoryByQuoteSettingQueryBuilder = new SQLiteQueryBuilder();

    //This is an inner join which looks like
    //quote INNER JOIN history ON quote.quote_id = history._id
    sHistoryByQuoteSettingQueryBuilder.setTables(
        Contract.History.TABLE_NAME + " INNER JOIN " +
            Contract.Quote.TABLE_NAME +
            " ON " + Contract.History.TABLE_NAME +
            "." + Contract.History.COLUMN_QUOTE_ID +
            " = " + Contract.Quote.TABLE_NAME +
            "." + Contract.Quote._ID);
  }

  private DbHelper dbHelper;

  @Override
  public boolean onCreate()
  {
    dbHelper = new DbHelper(getContext());
    return true;
  }

  @Nullable
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
  {
    Cursor returnCursor;
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    switch (uriMatcher.match(uri)) {
      case QUOTE:
        returnCursor = db.query(
            Contract.Quote.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );
        break;

      case QUOTE_FOR_SYMBOL:
        returnCursor = db.query(
            Contract.Quote.TABLE_NAME,
            projection,
            Contract.Quote.COLUMN_SYMBOL + " = ?",
            new String[] {Contract.Quote.getStockFromUri(uri)},
            null,
            null,
            sortOrder
        );

        break;
      case HISTORY:
        returnCursor = db.query(
            Contract.History.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );

        break;
      case HISTORY_FOR_SYMBOL:
        String symbol = getSymbolFromUri(uri);

        returnCursor = sHistoryByQuoteSettingQueryBuilder.query(
            db,
            projection,
            sQuoteSelection,
            new String[] {symbol},
            null,
            null,
            sortOrder);
        break;
      default:
        throw new UnsupportedOperationException("Unknown URI:" + uri);
    }

    returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

    //        if (db.isOpen()) {
    //            db.close();
    //        }

    return returnCursor;
  }

  @Nullable
  @Override
  public String getType(Uri uri)
  {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values)
  {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Uri returnUri;

    switch (uriMatcher.match(uri)) {
      case QUOTE: {
        long _id = db.insert(Contract.Quote.TABLE_NAME, null, values);
        if (_id > 0) { returnUri = Contract.Quote.buildQuoteUri(_id); } else {
          throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        break;
      }
      case HISTORY: {
        db.insert(
            Contract.History.TABLE_NAME,
            null,
            values
        );
        returnUri = Contract.History.uri;
        break;
      }
      default:
        throw new UnsupportedOperationException("Unknown URI:" + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null);

    return returnUri;
  }

  @Override
  public int bulkInsert(Uri uri, ContentValues[] values)
  {

    final SQLiteDatabase db = dbHelper.getWritableDatabase();

    switch (uriMatcher.match(uri)) {
      case QUOTE:
        db.beginTransaction();
        int returnCount = 0;
        try {
          for (ContentValues value : values) {
            db.insert(
                Contract.Quote.TABLE_NAME,
                null,
                value
            );
          }
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
      default:
        return super.bulkInsert(uri, values);
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs)
  {
    final SQLiteDatabase db = dbHelper.getWritableDatabase();
    int rowsDeleted;

    if (null == selection) selection = "1";
    switch (uriMatcher.match(uri)) {
      case QUOTE:
        rowsDeleted = db.delete(
            Contract.Quote.TABLE_NAME,
            selection,
            selectionArgs
        );

        break;

      case QUOTE_FOR_SYMBOL:
        String symbol = Contract.Quote.getStockFromUri(uri);
        Cursor cursor = db.query(
            Contract.Quote.TABLE_NAME,
            Contract.Quote.QUOTE_COLUMNS,
            null,
            null,
            null,
            null,
            null
        );
        if (cursor.moveToNext()) {
          int symbolId = cursor.getInt(Contract.Quote.POSITION_ID);
          db.delete(
              Contract.History.TABLE_NAME,
              Contract.History.COLUMN_QUOTE_ID + " = ?",
              new String[]{String.valueOf(symbolId)});
        }
        cursor.close();

        rowsDeleted = db.delete(
            Contract.Quote.TABLE_NAME,
            '"' + symbol + '"' + " =" + Contract.Quote.COLUMN_SYMBOL,
            selectionArgs
        );

        break;

      case HISTORY:
        rowsDeleted = db.delete(
            Contract.History.TABLE_NAME,
            selection,
            selectionArgs
        );
        break;
      default:
        throw new UnsupportedOperationException("Unknown URI:" + uri);
    }

    if (rowsDeleted != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
  {
    return 0;
  }

  static UriMatcher buildUriMatcher()
  {
    UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE, QUOTE);
    matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE_WITH_SYMBOL, QUOTE_FOR_SYMBOL);

    matcher.addURI(Contract.AUTHORITY, Contract.PATH_HISTORY, HISTORY);
    matcher.addURI(Contract.AUTHORITY, PATH_HISTORY_WITH_SYMBOL, HISTORY_FOR_SYMBOL);
    return matcher;
  }
}
