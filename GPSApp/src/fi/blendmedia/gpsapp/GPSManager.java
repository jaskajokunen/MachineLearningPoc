package fi.blendmedia.gpsapp;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class GPSManager {

	private static GPSManager sGPSManager;
	private static Context mAppContext;
	private LocationManager mLocationManager;
	private AlarmManager mAlarmManager;
	private ArrayList<GeofenceObject> mGeofenceObjects;

	/**
	 * Privaatti konstruktori, koska luokka on singleton
	 * 
	 * @param appContext
	 *            Context-olio
	 */
	private GPSManager(Context appContext) {

		mAppContext = appContext;

		// Haetaan LocationManager-luokka
		// LOCATION_SERVICE --> halutaan hallita sijainti-päivityksiä
		mLocationManager = (LocationManager) mAppContext
				.getSystemService(Context.LOCATION_SERVICE);
		
		mAlarmManager = (AlarmManager)mAppContext.getSystemService(Context.ALARM_SERVICE); 
	}

	/**
	 * Luo instanssin GPSManager-luokasta, jos sitä ei ole
	 * 
	 * @param c
	 *            Context-olio
	 * @return GPSManager-luokan instanssin
	 */
	public static GPSManager get(Context c) {

		if (sGPSManager == null) {
			sGPSManager = new GPSManager(c.getApplicationContext());
		}
		return sGPSManager;
	}

	/**
	 * Asettaa proximity alertit ja sijaintipäivitykset
	 * 
	 * @param context
	 *            Context-olio
	 */
	public void startLocationUpdates(Context context) {
		createGeoObjects();
		registerIntents();

		// Määritellään kriteerit, joiden perusteella valitaan provider
		Criteria criteria = new Criteria();

		// Määritellään providerin tarkkuus
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		// Haetaan kriteereitä parhaiten vastaa provider
		// getBestProvider() toinen parametri määrittelee, että palautetaan
		// provider, joka on enabled
		String provider = mLocationManager.getBestProvider(criteria, true);

		// Sijaintipäivitysten välinen minimi aika intervalli (ms)
		int timeToUpdate = 0;

		// Sijaintipäivitysten välinen minimi etäisyys
		int distance = 0;

		Log.d(GPSAppUtils.GPS_MANAGER_TAG, provider);

		// Haetaan viimeisin tiedetty sijainti
		Location lastKnown = mLocationManager.getLastKnownLocation(provider);

		if (lastKnown != null) {

			// Asetetaan nykyinen aika ajaksi
			lastKnown.setTime(System.currentTimeMillis());

			// Lähetetään sijainti
			broadcastLocation(lastKnown);
		}

		// Haetaan muuttujaan intent, joka broadcastataan
		// Parametri kertoo, että PendingIntent pitää tehdä
		PendingIntent pi = GPSAppUtils.getLocationPendingIntent(true,
				mAppContext);

		// Rekisteröidään sijaintipäivitykset annetulle PendingIntentille
		mLocationManager.requestLocationUpdates(provider, timeToUpdate,
				distance, pi);
	}

	/**
	 * Asettaa arraylistaan geofence oliot
	 */
	private void createGeoObjects() {
		mGeofenceObjects = new ArrayList<GeofenceObject>();
		// Ruoholahdenkatu 8
		mGeofenceObjects.add(new GeofenceObject(60.165898, 24.928503, 1));
		//mGeofenceObjects.add(new GeofenceObject(60.262986, 24.951769, 2));
	}

	/**
	 * Asettaa arraylistassa oleviin geofence olioihin proximity alertit
	 */
	private void registerIntents() {
		for (int i = 0; i < mGeofenceObjects.size(); i++) {
			setProximityAlert(mGeofenceObjects.get(i).getLatitude(),
					mGeofenceObjects.get(i).getLongitude(), mGeofenceObjects
							.get(i).getID(), i);
		}
	}

	/**
	 * Asettaa yhden proximity alertin geofence oliolle
	 * 
	 * @param lat
	 *            Geofence-olion latitude
	 * @param lon
	 *            Geofence-olion longitude
	 * @param id
	 *            Geofence-olion id
	 * @param requestCode
	 *            requestCoden avulla tunnistetaan request
	 */
	private void setProximityAlert(double lat, double lon, long id,
			int requestCode) {

		Log.d(GPSAppUtils.GPS_MANAGER_TAG, "set proximity alerts");

		// Geofencen säde
		float radius = 25000f;

		// Proximity alert ei umpeudu, jos arvo on -1
		long expiration = -1;
		
		Date date = new Date(System.currentTimeMillis());		
		long millis = date.getTime();

		Intent intent = new Intent(GPSAppUtils.PROXIMITY_ACTION);
		intent.putExtra(GPSAppUtils.EVENT_ID_INTENT_EXTRA, id);
		intent.putExtra(GPSAppUtils.TIMESTAMP_INTENT_EXTRA, millis);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mAppContext,
				requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		mLocationManager.addProximityAlert(lat, lon, radius, expiration,
				pendingIntent);
	}

	/**
	 * Poistaa geofence-olioista proximity alertit
	 */
	private void removeIntentRegister() {
		for (int i = 0; i < mGeofenceObjects.size(); i++) {
			removeProximityAlert(i);
		}
	}

	/**
	 * Poistaa annetun requestCoden avulla yhden proximity alertin
	 * 
	 * @param requestCode
	 *            requestCoden avulla tunnistetaan request
	 */
	private void removeProximityAlert(int requestCode) {

		// Log.d(TAG, "remove proximity alerts");

		Intent intent = new Intent(GPSAppUtils.PROXIMITY_ACTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mAppContext,
				requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		mLocationManager.removeProximityAlert(pendingIntent);
	}

	/**
	 * Lopettaa sijaintipäivitykset ja poistaa proximity alertit
	 */
	public void stopLocationUpdates() {

		// Haetaan muuttujaan intent, joka broadcastataan
		// false kertoo, että PendingIntenttia ei pidä tehdä
		PendingIntent pi = GPSAppUtils.getLocationPendingIntent(false,
				mAppContext);

		if (pi != null) {
			// Poistetaan sijaintipäivitykset pending intentista
			mLocationManager.removeUpdates(pi);
			removeIntentRegister();
			// Peruutetaan aktiivinen PendingIntent
			pi.cancel();
		}
	}

	/**
	 * Lähettää sijainnin sisältävän intentin BroadcastReceivereille
	 * 
	 * @param location
	 *            Location-olio
	 */
	private void broadcastLocation(Location location) {
		// Luodaan intent annetulla toiminnolla
		Intent intent = new Intent(GPSAppUtils.PROXIMITY_ACTION);

		// LocationReceiver-luokan onReceive metodissa voidaan hakea
		// intenttiin asetetetulla avaimella, sille asetettu arvo
		intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);

		// Lähetetään intent kiinnostuneille BroadcastReceivereille
		mAppContext.sendBroadcast(intent);
	}
}
