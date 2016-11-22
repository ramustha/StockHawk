package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
    SwipeRefreshLayout.OnRefreshListener
{
  public static final String EXTRA = "extra";
  private static final int DETAIL_LOADER = 0;
  @BindView(R.id.recycler_view_detail)
  RecyclerView recyclerView;
  @BindView(R.id.swipe_refresh)
  SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.symbol)
  TextView symbol;
  @BindView(R.id.error)
  TextView error;

  private Uri mUri;
  private String mSymbol;
  private DetailAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_detail);
    ButterKnife.bind(this);

    mUri = getIntent().getData();
    mSymbol = getIntent().getStringExtra(EXTRA);
    symbol.setText(mSymbol);
    swipeRefreshLayout.setOnRefreshListener(this);
    //QuoteSyncJob.initialize(this);
    getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);

    adapter = new DetailAdapter(this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    return new CursorLoader(getBaseContext(),
        mUri,
        Contract.History.HISTORY_COLUMNS,
        null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data)
  {
    swipeRefreshLayout.setRefreshing(false);
    adapter.setCursor(data);

    if (data.getCount() != 0) {
      error.setVisibility(View.GONE);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader)
  {
    swipeRefreshLayout.setRefreshing(false);
    adapter.setCursor(null);
  }

  @Override
  public void onRefresh()
  {
    QuoteSyncJob.syncImmediately(this);

    if (!networkUp() && adapter.getItemCount() == 0) {
      swipeRefreshLayout.setRefreshing(false);
      error.setText(getString(R.string.error_no_network));
      error.setVisibility(View.VISIBLE);
    } else if (!networkUp()) {
      swipeRefreshLayout.setRefreshing(false);
      Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
    } else if (PrefUtils.getStocks(this).size() == 0) {
      Timber.d("WHYAREWEHERE");
      swipeRefreshLayout.setRefreshing(false);
      error.setText(getString(R.string.error_no_stocks));
      error.setVisibility(View.VISIBLE);
    } else {
      error.setVisibility(View.GONE);
    }
  }

  private boolean networkUp()
  {
    ConnectivityManager cm =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnectedOrConnecting();
  }
}
