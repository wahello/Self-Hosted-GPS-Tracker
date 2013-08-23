package fr.herverenault.selfhostedgpstracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SelfHostedGPSTrackerActivity extends Activity implements LocationListener {

	private final static String MY_TAG = "SelfHostedGPSTrackerActivity";
	
	private LocationManager locationManager;
	
	private TextView text_gps_status;
	private TextView text_network_status;
	private ToggleButton button_toggle;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String gpsStatus = getString(bundle.getInt(SelfHostedGPSTrackerService.GPS_STATUS));
				Log.w(MY_TAG, "dans onReceive, gpsStatus == " + gpsStatus);
				text_gps_status.setText(gpsStatus);
				
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_self_hosted_gpstracker);

		text_gps_status = (TextView)findViewById(R.id.text_gps_status);
		text_network_status = (TextView)findViewById(R.id.text_network_status);
		button_toggle = (ToggleButton)findViewById(R.id.button_toggle);
	}

	@Override
	public void onResume() {
		super.onResume();
		// receive messages from the service
		registerReceiver(receiver, new IntentFilter(SelfHostedGPSTrackerService.NOTIFICATION));
		// current gps status
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 3, this); // TODO paramétrable !!!! (30 sec, 3 mètres)
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			onProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			onProviderDisabled(LocationManager.GPS_PROVIDER);
		}
		// is my service already running or has it stopped ?
		if (SelfHostedGPSTrackerService.isRunning) {
			Toast.makeText(this, "Service is running", Toast.LENGTH_SHORT).show(); // TODO toggle button..........
			button_toggle.setChecked(true);
		} else {
			Toast.makeText(this, "Service is NOT running", Toast.LENGTH_SHORT).show(); // TODO toggle button..........
			button_toggle.setChecked(false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
		locationManager.removeUpdates(this);
	}

	public void onToggleClicked(View view) {
	    boolean on = ((ToggleButton) view).isChecked();
		Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
	    if (on) {
			startService(intent);
	    } else {
			stopService(intent);
			text_gps_status.setText("");
			text_network_status.setText("");
	    }
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) { // TODO keep it even simpler : no need for settings activity !
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_self_hosted_gpstracker, menu);
		return true;
	} */
	
	/* -------------- GPS stuff -------------- */

	@Override
	public void onLocationChanged(Location location) {
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		text_gps_status.setText(getString(R.string.text_gps_status_disabled));
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		text_gps_status.setText(getString(R.string.text_gps_status_enabled));
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
