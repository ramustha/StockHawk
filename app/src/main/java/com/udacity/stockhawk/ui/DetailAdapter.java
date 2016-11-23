package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Ramustha on 11/21/2016.
 */

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailViewHolder>
{
  final private Context context;
  final private DecimalFormat dollarFormatWithPlus;
  final private DecimalFormat dollarFormat;
  final private DecimalFormat percentageFormat;
  final private SimpleDateFormat dateFormat;
  private Cursor cursor;

  DetailAdapter(Context context)
  {
    this.context = context;

    dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    dollarFormatWithPlus.setPositivePrefix("+$");
    percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
    percentageFormat.setMaximumFractionDigits(2);
    percentageFormat.setMinimumFractionDigits(2);
    percentageFormat.setPositivePrefix("+");
    dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
  }

  void setCursor(Cursor cursor)
  {
    this.cursor = cursor;
    notifyDataSetChanged();
  }

  String getSymbolAtPosition(int position)
  {

    cursor.moveToPosition(position);
    return cursor.getString(Contract.Quote.POSITION_SYMBOL);
  }

  @Override
  public DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
  {

    View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote_detail, parent, false);

    return new DetailViewHolder(item);
  }

  @Override
  public void onBindViewHolder(DetailViewHolder holder, int position)
  {
    cursor.moveToPosition(position);

    long date = cursor.getLong(Contract.History.POSITION_DATE);
    float open = cursor.getFloat(Contract.History.POSITION_OPEN);
    float close = cursor.getFloat(Contract.History.POSITION_CLOSE);
    float low = cursor.getFloat(Contract.History.POSITION_LOW);
    float high = cursor.getFloat(Contract.History.POSITION_HIGH);
    float adjClose = cursor.getFloat(Contract.History.POSITION_ADJ_CLOSE);
    float volume = cursor.getFloat(Contract.History.POSITION_VOLUME);

    String dateDecs = dateFormat.format(date);
    String openDesc = dollarFormat.format(open);
    String closeDesc = dollarFormat.format(close);
    String lowDesc = dollarFormat.format(low);
    String highDesc = dollarFormat.format(high);
    String adjCloseDesc = dollarFormat.format(adjClose);
    String volumeDesc = dollarFormat.format(volume);

    holder.date.setText(dateDecs);
    holder.open.setText(context.getString(R.string.pref_open_stock, openDesc));
    holder.close.setText(context.getString(R.string.pref_close_stock, closeDesc));
    holder.low.setText(context.getString(R.string.pref_low_stock, lowDesc));
    holder.high.setText(context.getString(R.string.pref_high_stock, highDesc));
    holder.adjClose.setText(context.getString(R.string.pref_adj_close_stock, adjCloseDesc));
    holder.volume.setText(context.getString(R.string.pref_volume_stock, volumeDesc));

    //content description
    holder.date.setContentDescription(context.getString(R.string.pref_date_desc, dateDecs));
    holder.open.setContentDescription(context.getString(R.string.pref_open_stock, openDesc));
    holder.close.setContentDescription(context.getString(R.string.pref_close_stock, closeDesc));
    holder.low.setContentDescription(context.getString(R.string.pref_low_stock, lowDesc));
    holder.high.setContentDescription(context.getString(R.string.pref_high_stock, highDesc));
    holder.adjClose.setContentDescription(context.getString(R.string.pref_adjs_close_stock, adjCloseDesc));
    holder.volume.setContentDescription(context.getString(R.string.pref_volume_stock, volumeDesc));
  }

  @Override
  public int getItemCount()
  {
    int count = 0;
    if (cursor != null) {
      count = cursor.getCount();
    }
    return count;
  }

  interface StockAdapterOnClickHandler
  {
    void onClick(String symbol);
  }

  class DetailViewHolder extends RecyclerView.ViewHolder
  {
    @BindView(R.id.list_item_date_textview)
    TextView date;
    @BindView(R.id.list_item_open_textview)
    TextView open;
    @BindView(R.id.list_item_close_textview)
    TextView close;
    @BindView(R.id.list_item_high_textview)
    TextView high;
    @BindView(R.id.list_item_low_textview)
    TextView low;
    @BindView(R.id.list_item_adj_close_textview)
    TextView adjClose;
    @BindView(R.id.list_item_volume_textview)
    TextView volume;

    DetailViewHolder(View itemView)
    {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
