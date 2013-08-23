package fr.herverenault.selfhostedgpstracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SelfHostedGPSTrackerActivity extends Activity implements OnClickListener {

	private final static String MY_TAG = "SelfHostedGPSTrackerActivity";
	
	private TextView text_gps_status;
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

		Button button_toggle = (Button)findViewById(R.id.button_toggle);
		button_toggle.setOnClickListener(this);

		text_gps_status = (TextView)findViewById(R.id.text_gps_status);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(SelfHostedGPSTrackerService.NOTIFICATION));
		Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
		startService(intent); // TODO toggle button !
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
		stopService(intent); // TODO toggle button !
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) { // TODO keep it even simpler : no need for settings activity !
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_self_hosted_gpstracker, menu);
		return true;
	} */
}
