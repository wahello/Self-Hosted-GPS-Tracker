package fr.herverenault.selfhostedgpstracker;

import java.text.DateFormat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SelfHostedGPSTrackerActivity extends Activity implements LocationListener {

	private final static String MY_TAG = "SelfHostedGPSTrackerActivity";
	private final static int PREFS_RESULT = 1;

	private LocationManager locationManager;

	private EditText edit_url;
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

		edit_url = (EditText)findViewById(R.id.edit_url);
		text_gps_status = (TextView)findViewById(R.id.text_gps_status);
		text_network_status = (TextView)findViewById(R.id.text_network_status);
		button_toggle = (ToggleButton)findViewById(R.id.button_toggle);
		text_running_since = (TextView)findViewById(R.id.text_running_since);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.contains("URL") && ! preferences.getString("URL", "").equals("")) {
			edit_url.setText(preferences.getString("URL", getString(R.string.hint_url)));
			edit_url.clearFocus();
		} else {
			edit_url.requestFocus();
		}
		edit_url.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Log.d(MY_TAG, "onTextChanged");
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				Log.d(MY_TAG, "beforeTextChanged");	
			}

			@Override
			public void afterTextChanged(Editable s) {
				Log.d(MY_TAG, "afterTextChanged");
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("URL", s.toString());
				editor.commit();
			}
		});
		// receive messages from the service
		registerReceiver(receiver, new IntentFilter(SelfHostedGPSTrackerService.NOTIFICATION));
		// current gps status
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		int pref_gps_updates = Integer.parseInt(preferences.getString("pref_gps_updates", "30")); // seconds		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, pref_gps_updates * 1000, 1, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			onProviderEnabled(LocationManager.GPS_PROVIDER);
		} else {
			onProviderDisabled(LocationManager.GPS_PROVIDER);
		}

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_self_hosted_gpstracker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			i = new Intent(this, SelfHostedGPSTrackerPrefs.class);
			startActivityForResult(i, PREFS_RESULT);
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PREFS_RESULT) {
			Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
			if (SelfHostedGPSTrackerService.isRunning) {
				Toast.makeText(this, "Restarting service", Toast.LENGTH_SHORT).show();
				stopService(intent);
				startService(intent);
			}
		}
	}

	public void onToggleClicked(View view) {
		Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
		if (((ToggleButton) view).isChecked()) {
			startService(intent);
		} else {
			stopService(intent);
		}
	}

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
