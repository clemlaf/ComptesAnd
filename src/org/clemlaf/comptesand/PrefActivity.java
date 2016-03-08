package org.clemlaf.comptesand;

import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.EditTextPreference;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class PrefActivity extends PreferenceActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
    @Override
    public void onResume(){
      super.onResume();
        sumtoValue(R.string.my_pref_home_hostname);
        sumtoValue(R.string.my_pref_out_hostname);
        sumtoValue(R.string.my_pref_crt_path);
        sumtoValue(R.string.my_pref_nbpage);
    }
    private void sumtoValue(int prid)
    {
        SharedPreferences sharedPref= PreferenceManager.getDefaultSharedPreferences(this);
        EditTextPreference et=(EditTextPreference) findPreference(getString(prid));
        String hostn=sharedPref.getString(getString(prid),"");
        if(hostn.length()>0){
            et.setSummary(hostn);
        }
    }
}
