package fi.blendmedia.gpsapp;

import android.app.Activity;
import android.os.Bundle;

/**
 * Luokka luo testik�yt��n tarkoitetun Activityn. Activity asettaa ainoastaaan
 * n�kym�lle sis�ll�n.
 * 
 */
public class DestinationActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.notification_layout);
	}
}
