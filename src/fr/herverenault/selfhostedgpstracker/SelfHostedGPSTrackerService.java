package fr.herverenault.selfhostedgpstracker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.widget.Toast;

public class SelfHostedGPSTrackerService extends Service implements LocationListener {

	private final static String MY_TAG = "SelfHostedGPSTrackerService";
	public static final String GPS_STATUS = "gps_status";
	public static final String NOTIFICATION = "fr.herverenault.selfhostedgpstracker";

	public static boolean isRunning;
	
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
			Log.w(MY_TAG, "dans ServiceHandler.handleMessage");
			// TODO paramètre : temps maximum de vie du service ! (24 heures pour le moment)
			long endTime = System.currentTimeMillis() + 10*1000; //24*60*60*1000;
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
		Log.w(MY_TAG, "dans onCreate");
		isRunning = true;

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			onProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			onProviderDisabled(LocationManager.GPS_PROVIDER);
		}
		
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // TODO useless ?
		if (location != null) {
			Log.w(MY_TAG, "last known location : " + location.getLatitude() + " " + location.getLongitude());
		} else {
			Log.w(MY_TAG, "last location unknown.");
		}
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 3, this); // TODO paramétrable !!!! (30 sec, 3 mètres)

		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler 
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, R.string.toast_started, Toast.LENGTH_SHORT).show();
		Log.w(MY_TAG, "dans onStartCommand");

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
		Log.w(MY_TAG, "service done");
		locationManager.removeUpdates(this);
		isRunning = false;
	}

	/* -------------- GPS stuff -------------- */

	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(this, location.getLatitude() + "\n" + location.getLongitude(), Toast.LENGTH_SHORT).show();
		Log.w(MY_TAG, "Dans onLocationChanged !!!!!!!!!!!!");
		try {
			URL url = new URL("http://herverenault.fr/gps?lat=" + location.getLatitude() + "&lon=" + location.getLongitude());	
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			int response = conn.getResponseCode();
			Log.w(MY_TAG, "Requête HTTP retourne : " + response);
		} catch (IOException e) {
			//e.printStackTrace();
			Log.w(MY_TAG, "Requête HTTP impossible !");
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(GPS_STATUS, R.string.text_gps_status_disabled);
		sendBroadcast(intent);
		Toast.makeText(this, R.string.text_gps_status_disabled, Toast.LENGTH_SHORT).show();
		Log.w(MY_TAG, "Dans onProviderDisabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(GPS_STATUS, R.string.text_gps_status_enabled);
		sendBroadcast(intent);
		Toast.makeText(this, R.string.text_gps_status_enabled, Toast.LENGTH_SHORT).show();
		Log.w(MY_TAG, "Dans onProviderEnabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(this, provider + " status " + status, Toast.LENGTH_SHORT).show();
		Log.w(MY_TAG, "Dans onStatusChanged " + status);
	}
}
