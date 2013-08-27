package fr.herverenault.selfhostedgpstracker;

import java.util.Calendar;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class SelfHostedGPSTrackerService extends IntentService implements LocationListener {

	private final static String MY_TAG = "SelfHostedGPSTrackerService";
	
	public static final String NOTIFICATION = "fr.herverenault.selfhostedgpstracker";

	public static boolean isRunning;
	public static Calendar runningSince;
	public static Calendar stoppedOn;

	private LocationManager locationManager;

	public SelfHostedGPSTrackerService() {
		super("SelfHostedGPSTrackerService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(MY_TAG, "in onCreate, init GPS stuff");
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			onProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			onProviderDisabled(LocationManager.GPS_PROVIDER);
		}
				
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		int pref_gps_updates = Integer.parseInt(preferences.getString("pref_gps_updates", "30")); // seconds
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, pref_gps_updates * 1000, 1, this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(MY_TAG, "in onHandleIntent, run for maximum time set in preferences");
		
		isRunning = true;
		runningSince = Calendar.getInstance();
		Intent i = new Intent(NOTIFICATION);
		sendBroadcast(i);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		int pref_max_run_time = Integer.parseInt(preferences.getString("pref_max_run_time", "24")); // hours

		long endTime = System.currentTimeMillis() + pref_max_run_time*60*60*1000;
		while (System.currentTimeMillis() < endTime) {
			synchronized (this) {
				try {
					wait(endTime - System.currentTimeMillis());
				} catch (Exception e) {
				}
			}
		}
	}
	
	@Override
	public void onDestroy() {
		// (user clicked the stop button, or max run time has been reached)
		Log.d(MY_TAG, "in onDestroy, stop listening to the GPS");

		locationManager.removeUpdates(this);
		
		isRunning = false;
		stoppedOn = Calendar.getInstance();
		Intent intent = new Intent(NOTIFICATION);
		sendBroadcast(intent);
	}

	/* -------------- GPS stuff -------------- */

	@Override
	public void onLocationChanged(Location location) {
		Log.d(MY_TAG, "in onLocationChanged !");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String urlText = preferences.getString("URL", "");
		if (urlText.contains("?")) {
			urlText = urlText + "&"; 
		} else {
			urlText = urlText + "?";
		}
		urlText = urlText + "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
		new SelfHostedGPSTrackerRequest().execute(urlText);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
