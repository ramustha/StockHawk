package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.udacity.stockhawk.sync.QuoteSyncJob;

/**
 * Created by Ramustha on 11/23/2016.
 */

public class TodayWidgetProvider extends AppWidgetProvider
{
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
  {
    context.startService(new Intent(context, TodayWidgetIntentService.class));
  }

  @Override
  public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
  {
    context.startService(new Intent(context, TodayWidgetIntentService.class));
  }

  @Override
  public void onReceive(Context context, Intent intent)
  {
    if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
      context.startService(new Intent(context, TodayWidgetIntentService.class));
    }
  }
}
