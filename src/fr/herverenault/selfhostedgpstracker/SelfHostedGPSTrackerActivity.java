package fr.herverenault.selfhostedgpstracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SelfHostedGPSTrackerActivity extends Activity {

	private final static String MY_TAG = "SelfHostedGPSTrackerActivity";
	
	private TextView text_gps_status;
	private TextView text_network_status;
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
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(SelfHostedGPSTrackerService.NOTIFICATION));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
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
}
