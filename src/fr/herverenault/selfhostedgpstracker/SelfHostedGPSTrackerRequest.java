package fr.herverenault.selfhostedgpstracker;

import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class SelfHostedGPSTrackerRequest extends AsyncTask<String, Void, Void> {
	protected Void doInBackground(String... urlText) {
		try {
			URL url = new URL(urlText[0]);	
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			int response = conn.getResponseCode();
		} catch (Exception e) { // we cannot do anything about that : network may be temporarily down
			Log.d(this.getClass().getName(), "Requête HTTP impossible");
			// e.printStackTrace();
		}
		return null;
	}
}
