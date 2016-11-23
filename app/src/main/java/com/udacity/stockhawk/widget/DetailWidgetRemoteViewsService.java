package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.DetailActivity;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Ramustha on 11/23/2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService
{
  final private DecimalFormat dollarFormat;
  final private DecimalFormat percentageFormat;

  DetailWidgetRemoteViewsService()
  {
    dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
    percentageFormat.setMaximumFractionDigits(2);
    percentageFormat.setMinimumFractionDigits(2);
    percentageFormat.setPositivePrefix("+");
  }

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent aIntent)
  {
    return new RemoteViewsFactory() {
      private Cursor data = null;

      @Override
      public void onCreate() {
        // Nothing to do
      }

      @Override
      public void onDataSetChanged() {
        if (data != null) {
          data.close();
        }

        final long identityToken = Binder.clearCallingIdentity();
        data = getContentResolver().query(Contract.Quote.uri,
            Contract.Quote.QUOTE_COLUMNS,
            null,
            null,
            Contract.Quote.COLUMN_SYMBOL);
        Binder.restoreCallingIdentity(identityToken);
      }

      @Override
      public void onDestroy() {
        if (data != null) {
          data.close();
          data = null;
        }
      }

      @Override
      public int getCount() {
        return data == null ? 0 : data.getCount();
      }

      @Override
      public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
            data == null || !data.moveToPosition(position)) {
          return null;
        }
        RemoteViews views = new RemoteViews(getPackageName(),
            R.layout.widget_detail_list_item);

        String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
        float rawPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        String percentage = percentageFormat.format(percentageChange / 100);
        String price = dollarFormat.format(rawPrice);

        views.setTextViewText(R.id.symbol, symbol);
        views.setTextViewText(R.id.price, price);
        views.setTextViewText(R.id.change, percentage);

        final Intent fillInIntent = new Intent();

        Uri contentUri = Contract.History.makeUriForSymbol(symbol);

        fillInIntent.setData(contentUri).putExtra(DetailActivity.EXTRA, symbol);
        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
        return views;
      }

      @Override
      public RemoteViews getLoadingView() {
        return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
      }

      @Override
      public int getViewTypeCount() {
        return 1;
      }

      @Override
      public long getItemId(int position) {
        if (data.moveToPosition(position))
          return data.getLong(Contract.Quote.POSITION_ID);
        return position;
      }

      @Override
      public boolean hasStableIds() {
        return true;
      }
    };
  }
}
