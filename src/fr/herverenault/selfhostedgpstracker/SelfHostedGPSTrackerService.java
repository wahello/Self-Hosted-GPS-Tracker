package fr.herverenault.selfhostedgpstracker;

import java.util.Calendar;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
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

	public static final String NOTIFICATION = "fr.herverenault.selfhostedgpstracker";

	public static boolean isRunning;
	public static Calendar runningSince;
	public Calendar stoppedOn;

	private final static String MY_TAG = "SelfHostedGPSTrackerService";
	
	private SharedPreferences preferences;
	private String urlText;
	private LocationManager locationManager;
	private int pref_gps_updates;
	private long latestUpdate;
	private int pref_max_run_time;

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
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("stoppedOn", 0);
		editor.commit();
		pref_gps_updates = Integer.parseInt(preferences.getString("pref_gps_updates", "30")); // seconds
		pref_max_run_time = Integer.parseInt(preferences.getString("pref_max_run_time", "24")); // hours
		urlText = preferences.getString("URL", "");
		if (urlText.contains("?")) {
			urlText = urlText + "&"; 
		} else {
			urlText = urlText + "?";
		}
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, pref_gps_updates * 1000, 1, this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(MY_TAG, "in onHandleIntent, run for maximum time set in preferences");
		new SelfHostedGPSTrackerRequest().execute(urlText + "tracker=start");
				
		isRunning = true;
		runningSince = Calendar.getInstance();
		Intent i = new Intent(NOTIFICATION);
		sendBroadcast(i);
		
		Notification notification = new Notification(R.drawable.ic_notif, getText(R.string.toast_service_running), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, SelfHostedGPSTrackerActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, getText(R.string.app_name), getText(R.string.toast_service_running), pendingIntent);
		startForeground(R.id.logo, notification);
				
		long endTime = System.currentTimeMillis() + pref_max_run_time*60*60*1000;
		while (System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(60*1000); // note: when device is sleeping, it may last up to 5 minutes or more
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public void onDestroy() {
		// (user clicked the stop button, or max run time has been reached)
		Log.d(MY_TAG, "in onDestroy, stop listening to the GPS");
		new SelfHostedGPSTrackerRequest().execute(urlText + "tracker=stop");
		
		locationManager.removeUpdates(this);
		
		isRunning = false;
		stoppedOn = Calendar.getInstance();
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("stoppedOn", stoppedOn.getTimeInMillis());
		editor.commit();
		
		Intent intent = new Intent(NOTIFICATION);
		sendBroadcast(intent);
	}

	/* -------------- GPS stuff -------------- */

	@Override
	public void onLocationChanged(Location location) {
		Log.d(MY_TAG, "in onLocationChanged, latestUpdate == " + latestUpdate);
		
		if ((System.currentTimeMillis() - latestUpdate) < pref_gps_updates*1000) {
			return;
		} else {
			latestUpdate = System.currentTimeMillis();
		}
		
		new SelfHostedGPSTrackerRequest().execute(urlText + "lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
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
