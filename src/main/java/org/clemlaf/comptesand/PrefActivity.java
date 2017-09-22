package org.clemlaf.comptesand;

import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.EditTextPreference;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class PrefActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
  SharedPreferences sharedPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref= PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onResume(){
      super.onResume();
      updateSum();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String k){
      updateSum();
    }
    private void updateSum()
    {
        sumtoValue(R.string.my_pref_home_hostname);
        sumtoValue(R.string.my_pref_out_hostname);
        sumtoValue(R.string.my_pref_crt_path);
        sumtoValue(R.string.my_pref_nbpage);
    }
    private void sumtoValue(int prid)
    {
        EditTextPreference et=(EditTextPreference) findPreference(getString(prid));
        String hostn=sharedPref.getString(getString(prid),"");
        if(hostn.length()>0){
            et.setSummary(hostn);
        }
    }
}
