package fr.herverenault.selfhostedgpstracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelfHostedGPSTrackerActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_self_hosted_gpstracker);
		Button button_stop = (Button)findViewById(R.id.button_stop);
		button_stop.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
		startService(intent);
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, SelfHostedGPSTrackerService.class);
		stopService(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_self_hosted_gpstracker, menu);
		return true;
	}
}
