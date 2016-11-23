package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
  public static final String EXTRA = "extra";
  private static final int DETAIL_LOADER = 0;
  @BindView(R.id.recycler_view_detail)
  RecyclerView recyclerView;
  @BindView(R.id.progressbar)
  ProgressBar progressBar;
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
    getSupportActionBar().setElevation(0f);
    ButterKnife.bind(this);

    mUri = getIntent().getData();
    mSymbol = getIntent().getStringExtra(EXTRA);
    symbol.setText(mSymbol);

    getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);

    adapter = new DetailAdapter(this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);

    progressBar.setVisibility(View.VISIBLE);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    progressBar.setVisibility(View.VISIBLE);
    return new CursorLoader(getBaseContext(),
        mUri,
        Contract.History.HISTORY_COLUMNS,
        null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data)
  {
    adapter.setCursor(data);

    if (data.getCount() != 0) {
      error.setVisibility(View.GONE);
      progressBar.setVisibility(View.GONE);
    }else {
      progressBar.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader)
  {
    progressBar.setVisibility(View.VISIBLE);
    adapter.setCursor(null);
  }
}
