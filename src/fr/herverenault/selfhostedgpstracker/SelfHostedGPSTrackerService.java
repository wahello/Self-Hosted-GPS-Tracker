package fr.herverenault.selfhostedgpstracker;

import java.util.Calendar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SelfHostedGPSTrackerService extends Service implements LocationListener {

	private final static String MY_TAG = "SelfHostedGPSTrackerService";
	
	public static final String NOTIFICATION = "fr.herverenault.selfhostedgpstracker";

	public static boolean isRunning;
	public static Calendar runningSince;
	public static Calendar stoppedOn;
	
	// http://developer.android.com/guide/components/services.html#ExtendingService
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	private LocationManager locationManager;

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
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
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			stopSelf(msg.arg1);
		}
	}

	@Override
	public void onCreate() {
		Log.d(MY_TAG, "in onCreate");
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			onProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			onProviderDisabled(LocationManager.GPS_PROVIDER);
		}
				
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		int pref_gps_updates = Integer.parseInt(preferences.getString("pref_gps_updates", "30")); // seconds
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, pref_gps_updates * 1000, 1, this);

		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler 
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		isRunning = true;
		runningSince = Calendar.getInstance();
		Intent intent = new Intent(NOTIFICATION);
		sendBroadcast(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, R.string.toast_started, Toast.LENGTH_SHORT).show();
		Log.d(MY_TAG, "dans onStartCommand");

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, R.string.toast_stopped, Toast.LENGTH_SHORT).show();
		Log.d(MY_TAG, "in onDestroy");

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
