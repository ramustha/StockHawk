package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob
{
  public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
  static final int ONE_OFF_ID = 2;
  private static final int PERIOD = 300000;
  private static final int INITIAL_BACKOFF = 10000;
  private static final int PERIODIC_ID = 1;

  static void getQuotes(Context context)
  {
    Timber.d("Running sync job");

    Calendar from = Calendar.getInstance();
    Calendar to = Calendar.getInstance();
    from.add(Calendar.YEAR, -1);

    //remove temp history
    Cursor cursor = context.getContentResolver().query(Contract.Quote.uri, Contract.Quote.QUOTE_COLUMNS, null, null, null);
    if (cursor.moveToNext()) {
      int id = cursor.getInt(Contract.Quote.POSITION_ID);
      context.getContentResolver().delete(
          Contract.History.uri,
          Contract.History.COLUMN_QUOTE_ID + " != ?",
          new String[]{String.valueOf(id)});
    }else {
      context.getContentResolver().delete(
          Contract.History.uri,
          null,
          null);
    }
    cursor.close();

    try {

      Set<String> stockPref = PrefUtils.getStocks(context);
      Set<String> stockCopy = new HashSet<>();
      stockCopy.addAll(stockPref);
      String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

      Timber.d(stockCopy.toString());

      if (stockArray.length == 0) {
        return;
      }

      Map<String, Stock> quotes = YahooFinance.get(stockArray);
      Iterator<String> iterator = stockCopy.iterator();

      Timber.d("Quotes %s", quotes.toString());

      while (iterator.hasNext()) {
        String symbol = iterator.next();

        Stock stock = quotes.get(symbol);
        StockQuote quote = stock.getQuote();
        if (quote.getPrice() != null && quote.getChange() != null) {

          float price = quote.getPrice().floatValue();
          float change = quote.getChange().floatValue();
          float percentChange = quote.getChangeInPercent().floatValue();

          ContentValues quoteCV = new ContentValues();
          quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
          quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
          quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
          quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);

          Uri insertedQuoteUri = context.getContentResolver()
              .insert(Contract.Quote.uri, quoteCV);

          long quoteId = ContentUris.parseId(insertedQuoteUri);

          // WARNING! Don't request historical data for a stock that doesn't exist!
          // The request will hang forever X_x
          List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

          ArrayList<ContentValues> historyCVs = new ArrayList<>();

          for (HistoricalQuote it : history) {
            ContentValues historyValues = new ContentValues();
            historyValues.put(Contract.History.COLUMN_QUOTE_ID, quoteId);
            historyValues.put(Contract.History.COLUMN_DATE, it.getDate().getTimeInMillis());
            historyValues.put(Contract.History.COLUMN_OPEN, it.getOpen().floatValue());
            historyValues.put(Contract.History.COLUMN_CLOSE, it.getClose().floatValue());
            historyValues.put(Contract.History.COLUMN_LOW, it.getLow().floatValue());
            historyValues.put(Contract.History.COLUMN_HIGH, it.getHigh().floatValue());
            historyValues.put(Contract.History.COLUMN_ADJ_CLOSE, it.getAdjClose().floatValue());
            historyValues.put(Contract.History.COLUMN_VOLUME, it.getVolume());
            historyCVs.add(historyValues);
          }

          context.getContentResolver().bulkInsert(
              Contract.History.uri, historyCVs.toArray(new ContentValues[historyCVs.size()]));
        }

        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        context.sendBroadcast(dataUpdatedIntent);
      }
    } catch (IOException exception) {
      Timber.e(exception, "Error fetching stock quotes");
    }
  }

  private static void schedulePeriodic(Context context)
  {
    Timber.d("Scheduling a periodic task");

    JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));

    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setPeriodic(PERIOD)
        .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

    JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

    scheduler.schedule(builder.build());
  }

  synchronized public static void initialize(final Context context)
  {
    schedulePeriodic(context);
    syncImmediately(context);
  }

  synchronized public static void syncImmediately(Context context)
  {

    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
      Intent nowIntent = new Intent(context, QuoteIntentService.class);
      context.startService(nowIntent);
    } else {

      JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));

      builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
          .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

      JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

      scheduler.schedule(builder.build());
    }
  }
}
