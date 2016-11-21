package com.udacity.stockhawk.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.udacity.stockhawk.R;

/**
 * Created by Ramustha on 11/21/2016.
 */

public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener
{

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
    Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
    root.addView(bar, 0); // insert at top
    bar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    // Add 'general' preferences, defined in the XML file
    addPreferencesFromResource(R.xml.pref_general);

    // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
    // updated when the preference changes.
    bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_fonts_key)));
  }

  // Registers a shared preference change listener that gets notified when preferences change
  @Override
  protected void onResume() {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    sp.registerOnSharedPreferenceChangeListener(this);
    super.onResume();
  }

  // Unregisters a shared preference change listener
  @Override
  protected void onPause() {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    sp.unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences aSharedPreferences, String akey)
  {
    if ( akey.equals(getString(R.string.pref_fonts_key)) ) {
      // fonts have changed. update fonts accordingly
      //getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
    }
  }

  @Override
  public boolean onPreferenceChange(Preference aPreference, Object aValue)
  {
    setPreferenceSummary(aPreference, aValue);
    return true;
  }

  /**
   * Attaches a listener so the summary is always updated with the preference value.
   * Also fires the listener once, to initialize the summary (so it shows up before the value
   * is changed.)
   */
  private void bindPreferenceSummaryToValue(Preference preference) {
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(this);

    // Set the preference summaries
    setPreferenceSummary(preference,
        PreferenceManager
            .getDefaultSharedPreferences(preference.getContext())
            .getString(preference.getKey(), ""));
  }

  private void setPreferenceSummary(Preference preference, Object value) {
    String stringValue = value.toString();
    String key = preference.getKey();

    if (preference instanceof ListPreference) {
      // For list preferences, look up the correct display value in
      // the preference's 'entries' list (since they have separate labels/values).
      ListPreference listPreference = (ListPreference) preference;
      int prefIndex = listPreference.findIndexOfValue(stringValue);
      if (prefIndex >= 0) {
        preference.setSummary(listPreference.getEntries()[prefIndex]);
      }
    } else {
      // For other preferences, set the summary to the value's simple string representation.
      preference.setSummary(stringValue);
    }
  }
}
