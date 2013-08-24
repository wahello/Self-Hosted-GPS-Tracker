package fr.herverenault.selfhostedgpstracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SelfHostedGPSTrackerPrefs extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference pref;
		
		pref = findPreference("pref_gps_updates");
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue == null 
						|| newValue.toString().length() == 0 
						|| Integer.parseInt(newValue.toString()) < 30) { // user has been warned
			        Toast.makeText(getApplicationContext(), getString(R.string.pref_gps_updates_too_low), Toast.LENGTH_SHORT).show();
			        return false;
				} else if (SelfHostedGPSTrackerService.isRunning) {
					Toast.makeText(getApplicationContext(), getString(R.string.toast_prefs_restart), Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		
		pref = findPreference("pref_max_run_time");
		pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) { // hours
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				int pref_gps_updates = Integer.parseInt(preferences.getString("pref_gps_updates", "0")); // seconds
				if (newValue == null 
						|| newValue.toString().length() == 0 
						|| (Integer.parseInt(newValue.toString()) * 3600) < pref_gps_updates) { // would not make sense...
			        Toast.makeText(getApplicationContext(), getString(R.string.pref_max_run_time_too_low), Toast.LENGTH_SHORT).show();
			        return false;
				} else if (SelfHostedGPSTrackerService.isRunning) {
					Toast.makeText(getApplicationContext(), getString(R.string.toast_prefs_restart), Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
	}
}
