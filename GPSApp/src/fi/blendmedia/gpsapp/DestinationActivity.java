package fi.blendmedia.gpsapp;

import android.app.Activity;
import android.os.Bundle;

/**
 * Luokka luo testikäytöön tarkoitetun Activityn. Activity asettaa ainoastaaan
 * näkymälle sisällön.
 * 
 */
public class DestinationActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.notification_layout);
	}
}
