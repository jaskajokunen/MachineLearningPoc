package fi.blendmedia.gpsapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/**
 * Sovelluksen MainActivity. Activity on sovelluksen komponentti, joka tarjoaa
 * näytön, jonka kautta käyttäjä voi vuorovaikuttaa sovelluksen kanssa.
 * 
 */
public class GPSActivity extends Activity {

	private GPSManager mGPSManager;

	private Location mLastLocation;

	private TextView mAlertTextView;

	private Context mAppContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LocationReceiver.setMainActivity(this);

		mAppContext = getApplicationContext();

		// Asetetaan näkymälle layout
		setContentView(R.layout.activity_gps);

		// Haetaan resurssit id:n avulla
		mAlertTextView = (TextView) findViewById(R.id.alert_textView);

		mGPSManager = GPSManager.get(getApplicationContext());

		// Rekisteröidään BroadcastReceiver ajettavaksi main activityn säikeessä
		// IntentFilterin avulla määritellään, mistä actioneista Broadcast
		// Receiver on kiinnostunut
		/*
		 * registerReceiver(mLocationreceiver, new IntentFilter(
		 * GPSAppUtils.PROXIMITY_ACTION));
		 */

		mGPSManager.startLocationUpdates(getApplicationContext());
	}

	@Override
	public void onResume() {
		VisibilityManager.setIsVisible(true);
		super.onResume();
	}

	@Override
	public void onPause() {

		VisibilityManager.setIsVisible(false);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mGPSManager.stopLocationUpdates();

		// unregisterReceiver(mLocationreceiver);

	}

}
