package com.udacity.stockhawk.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract
{

  public static final String AUTHORITY = "com.udacity.stockhawk";

  public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

  public static final String PATH_QUOTE = "quote";
  public static final String PATH_QUOTE_WITH_SYMBOL = "quote/*";

  public static final String PATH_HISTORY = "history";
  public static final String PATH_HISTORY_WITH_SYMBOL = "history/*";

  public static final class Quote implements BaseColumns
  {

    public static final Uri uri = BASE_URI.buildUpon().appendPath(PATH_QUOTE).build();

    public static final String TABLE_NAME = "quotes";

    public static final String COLUMN_SYMBOL = "symbol";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_ABSOLUTE_CHANGE = "absolute_change";
    public static final String COLUMN_PERCENTAGE_CHANGE = "percentage_change";

    public static final int POSITION_ID = 0;
    public static final int POSITION_SYMBOL = 1;
    public static final int POSITION_PRICE = 2;
    public static final int POSITION_ABSOLUTE_CHANGE = 3;
    public static final int POSITION_PERCENTAGE_CHANGE = 4;

    public static final String[] QUOTE_COLUMNS = {
        TABLE_NAME + "." + _ID,
        COLUMN_SYMBOL,
        COLUMN_PRICE,
        COLUMN_ABSOLUTE_CHANGE,
        COLUMN_PERCENTAGE_CHANGE
    };

    public static Uri buildQuoteUri(long id)
    {
      return ContentUris.withAppendedId(uri, id);
    }

    public static Uri makeUriForStock(String symbol)
    {
      return uri.buildUpon().appendPath(symbol).build();
    }

    public static String getStockFromUri(Uri uri)
    {
      return uri.getLastPathSegment();
    }
  }

  public static final class History implements BaseColumns
  {

    public static final Uri uri = BASE_URI.buildUpon().appendPath(PATH_HISTORY).build();

    public static final String TABLE_NAME = "history";

    public static final String COLUMN_QUOTE_ID = "quote_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_OPEN = "open";
    public static final String COLUMN_CLOSE = "close";
    public static final String COLUMN_LOW = "low";
    public static final String COLUMN_HIGH = "high";
    public static final String COLUMN_ADJ_CLOSE = "adj_close";
    public static final String COLUMN_VOLUME = "volume";

    public static final int POSITION_ID = 0;
    public static final int POSITION_QUOTE_ID = 1;
    public static final int POSITION_DATE = 2;
    public static final int POSITION_OPEN = 3;
    public static final int POSITION_CLOSE = 4;
    public static final int POSITION_LOW = 5;
    public static final int POSITION_HIGH = 6;
    public static final int POSITION_ADJ_CLOSE = 7;
    public static final int POSITION_VOLUME = 8;

    public static final String[] HISTORY_COLUMNS = {
        TABLE_NAME + "." + _ID,
        COLUMN_QUOTE_ID,
        COLUMN_DATE,
        COLUMN_OPEN,
        COLUMN_CLOSE,
        COLUMN_LOW,
        COLUMN_HIGH,
        COLUMN_ADJ_CLOSE,
        COLUMN_VOLUME
    };

    public static Uri buildHistoryUri(long id)
    {
      return ContentUris.withAppendedId(uri, id);
    }

    public static Uri makeUriForSymbol(String symbol)
    {
      return uri.buildUpon().appendPath(symbol).build();
    }

    public static String getSymbolFromUri(Uri uri)
    {
      return uri.getPathSegments().get(1);
    }
  }
}
