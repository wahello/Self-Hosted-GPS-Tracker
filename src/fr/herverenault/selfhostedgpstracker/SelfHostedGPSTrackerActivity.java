package fr.herverenault.selfhostedgpstracker;

import java.text.DateFormat;

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
	private TextView text_running_since;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(MY_TAG, "dans onReceive ! appelée uniquement pour demander d'updater le status du service");
			updateServiceStatus();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_self_hosted_gpstracker);

		text_gps_status = (TextView)findViewById(R.id.text_gps_status);
		text_network_status = (TextView)findViewById(R.id.text_network_status);
		button_toggle = (ToggleButton)findViewById(R.id.button_toggle);
		text_running_since = (TextView)findViewById(R.id.text_running_since);
		
		// receive messages from the service
		registerReceiver(receiver, new IntentFilter(SelfHostedGPSTrackerService.NOTIFICATION));
		// current gps status
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 3, this); // TODO paramétrable !!!! (30 sec, 3 mètres)
	}

	@Override
	public void onResume() {
		super.onResume();
		updateServiceStatus();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		unregisterReceiver(receiver);
	}

	public void onToggleClicked(View view) {
		Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
	    if (((ToggleButton) view).isChecked()) {
			startService(intent);
	    } else {
			stopService(intent);
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
		Log.d(MY_TAG, "GPS disabled !");
		text_gps_status.setText(getString(R.string.text_gps_status_disabled));
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(MY_TAG, "GPS enabled !");
		text_gps_status.setText(getString(R.string.text_gps_status_enabled));
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	/* ----------- utility methods -------------- */
	private void updateServiceStatus() { 
		if (SelfHostedGPSTrackerService.isRunning) {
			Toast.makeText(this, "Service is running", Toast.LENGTH_SHORT).show();
			button_toggle.setChecked(true);
			text_running_since.setText(getString(R.string.text_running_since) + " " 
					+ DateFormat.getDateTimeInstance().format(SelfHostedGPSTrackerService.runningSince.getTime()));
		} else {
			Toast.makeText(this, "Service is NOT running", Toast.LENGTH_SHORT).show();
			button_toggle.setChecked(false);
			if (SelfHostedGPSTrackerService.stoppedOn != null) {
				text_running_since.setText(getString(R.string.text_stopped_on) + " " 
						+ DateFormat.getDateTimeInstance().format(SelfHostedGPSTrackerService.stoppedOn.getTime()));
			}
		}
	}
}
