package fi.blendmedia.gpsapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

public class LocationReceiver extends BroadcastReceiver {

	private GPSManager mGPSManager;
	private Context mAppContext;
	public static GPSActivity activity;
	TextView view;

	public static void setMainActivity(GPSActivity gpsActivity) {
		activity = gpsActivity;

	}

	// Metodia kutsutaan, kun BroadcastReceiver on saamaisillaan Intent
	// broadcastin
	// Intent on viesinvälitys-mekanismi, jolla on mahdollista mm. käynnistää
	// tietty Service/Activity, käynnistää Activity/Service suorittamaan jokin
	// action datan osalle, tai broadcastata, että event on tapahtunut
	@Override
	public void onReceive(Context context, Intent intent) {

		// Viittausten hakeminen luokkiin
		mAppContext = context;
		AlarmManager mAlarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		mGPSManager = GPSManager.get(context);

		// Intentilla on KEY_PROXIMITY_ENTERING-avain, jos laite on lähellä
		// proximity alertin sisältävää geofencea
		if (intent.hasExtra(LocationManager.KEY_PROXIMITY_ENTERING)) {
			Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG,
					"Received KEY_PROXIMITY_ENTERING");

			long eventID = intent.getLongExtra(
					GPSAppUtils.EVENT_ID_INTENT_EXTRA, -1);

			String locationString = "empty";

			long timestampMillis = intent.getLongExtra(
					GPSAppUtils.TIMESTAMP_INTENT_EXTRA, -2);

			SharedPreferences prefs = context.getSharedPreferences(
					GPSAppUtils.PREFS_TIME, 0);

			long restoredTime = prefs.getLong("proximityAlert", -1);
			long restoredTodayLong = prefs.getLong("today", -1);

			// Jos avaimella ei löytynyt tallennettua preferences dataa
			if (restoredTime == -1 || restoredTodayLong == -1) {
				savePreferencesData(context, timestampMillis);

				try {
					connectToService("ML");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (eventID == 1) {
				locationString = "Work";
			}

			else {
				locationString = "Something else";
			}

			if (intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING,
					false)) {

				onEnteringProximity(context, locationString);
			} else {
				onExitingProximity(context, locationString);
			}
		}

		// Intentilla on KEY_LOCATION_CHANGED-avain, jos on tullut
		// sijaintipäivitys
		else if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {

			Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG,
					"Received KEY_LOCATION_CHANGED");

			SharedPreferences prefs = mAppContext.getSharedPreferences(
					GPSAppUtils.PREFS_TIME, 0);

			long restoredTime = prefs.getLong("proximityAlert", -1);
			long restoredTodayLong = prefs.getLong("today", -1);

			// Jos avaimella löytyi tallennettua preferences dataa
			if (restoredTime != -1 || restoredTodayLong != -1) {

				DateFormat df = new SimpleDateFormat("dd");

				Calendar currentTime = Calendar.getInstance();
				Date restoredTodayDate = getDate(prefs, "today");

				String todayMonthDay = df.format(restoredTodayDate);
				String currentMonthDay = df.format(currentTime.getTime());

				// Katsotaan, onko päivä vaihtunut
				if (currentMonthDay.equals(todayMonthDay)) {
					Log.d(GPSAppUtils.GPS_MANAGER_TAG,
							"Nykyinen päivä on sama kuin tallennettu päivä");

					float timeToAlarm = prefs.getFloat("timeToAlarm", -1);

					boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
							new Intent(GPSAppUtils.ALARM_ACTION),
							PendingIntent.FLAG_NO_CREATE) != null);

					if (timeToAlarm != -1) {

						if (!alarmUp) {
							GPSAppUtils.setAlarm(timeToAlarm, mAlarmManager,
									context);
						}
						mGPSManager.stopLocationUpdates();
					}

				}

				// Tyhjennetään varasto, jos päivä vaihtui
				else {

					GPSAppUtils.clearPrefs(prefs);
				}

			}
		}

		if (intent.getAction().equals(GPSAppUtils.ALARM_ACTION)) {
			Log.d(GPSAppUtils.GPS_MANAGER_TAG, "Hälytykset alkoivat!");

			try {
				connectToService("HSL");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			SharedPreferences prefs = mAppContext.getSharedPreferences(
					GPSAppUtils.PREFS_TIME, 0);
			String HSLAPIObjectsString = prefs
					.getString("HSLObjectsList", null);

			if (!HSLAPIObjectsString.isEmpty()) {

				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {

						SharedPreferences prefs = mAppContext
								.getSharedPreferences(GPSAppUtils.PREFS_TIME, 0);
						String HSLAPIObjectsString = prefs.getString(
								"HSLObjectsList", null);

						Gson gson = new Gson();

						HSLAPIObject[] objArray = gson.fromJson(
								HSLAPIObjectsString, HSLAPIObject[].class);

						List<HSLAPIObject> hslObjects = Arrays.asList(objArray);

						String alarmString = GPSAppUtils
								.construcAlarmString(hslObjects);

						view = (TextView) activity
								.findViewById(R.id.alert_textView);
						view.setText(alarmString);

					}
				});
			}

			float timeToAlarm = prefs.getFloat("timeToAlarm", -1);

			int hours = (int) timeToAlarm;
			int minutes = (int) ((timeToAlarm - hours) * 60);

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hours);
			cal.set(Calendar.MINUTE, minutes);

			Log.d(GPSAppUtils.GPS_MANAGER_TAG,
					Long.toString(System.currentTimeMillis()));

			if (System.currentTimeMillis() >= cal.getTimeInMillis()) {

				GPSAppUtils.cancelAlarm(context, mAlarmManager);
			}

		}

	}

	private void savePreferencesData(Context context, long millis) {

		SharedPreferences prefs = context.getSharedPreferences(
				GPSAppUtils.PREFS_TIME, 0);
		SharedPreferences.Editor prefEditor = prefs.edit();
		prefEditor.putLong("proximityAlert", millis);

		Calendar c = Calendar.getInstance();
		long todayInMilliseconds = c.getTimeInMillis();
		prefEditor.putLong("today", todayInMilliseconds);

		prefEditor.commit();
	}

	public static Date getDate(final SharedPreferences prefs, final String key) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(prefs.getLong(key, 0));
		return calendar.getTime();
	}

	/**
	 * Metodia kutsutaan, kun saadaan sijaintipäivitys.
	 * 
	 * @param context
	 *            Context-olio
	 * @param location
	 *            Location-olio
	 */
	protected void onLocationReceived(Context context, Location location) {
		Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, "New location received");
	}

	/**
	 * Metodia kutsutaan, kun providerin enabled-tila muuttuu.
	 * 
	 * @param enabled
	 *            boolean, joka kertoo providerin enabled-tilan
	 * @param context
	 *            Context-olio
	 */
	protected void onProviderEnabledChanged(boolean enabled, Context context) {
		// Jos parametrina annettu enabled on true, niin arvo on "enabled"
		Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, "Provider "
				+ (enabled ? "enabled" : "disabled"));
	}

	/**
	 * Kutsutaan, jos laite on proximity alertin säteen sisällä.
	 * 
	 * @param context
	 *            Context-olio
	 * @param locationString
	 *            Proximity alertin sijaintia esittävä string
	 */
	public void onEnteringProximity(Context context, String locationString) {
		GPSAppUtils.displayNotification(context, "Entering Proximity",
				GPSAppUtils.NOTIFICATION_ID, locationString);
	}

	/**
	 * Kutsutaan, jos laite poistuu proximity alertin säteen sisältä.
	 * 
	 * @param context
	 *            Context-olio
	 * @param locationString
	 *            Proximity alertin sijaintia esittävä string
	 */
	public void onExitingProximity(Context context, String locationString) {
		GPSAppUtils.displayNotification(context, "Exiting Proximity",
				GPSAppUtils.NOTIFICATION_ID, locationString);
	}

	/**
	 * Yhdistää serviceen, jos internet-yhteys on saatavilla.
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private void connectToService(String serviceToConnect)
			throws UnsupportedEncodingException {

		// ConnectivityManagerin avulla on mahdollista tarkastaa, onko laite
		// yhdistetty Internettiin
		ConnectivityManager connMgr = (ConnectivityManager) mAppContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		// Haetaan tiedot nykyisestä verkosta
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		// Tarkastetaan, onko verkkoyhteyttä saatavilla
		// networkInfo on tyhjä, jos ei ole oletusverkkoa
		if (networkInfo != null && networkInfo.isConnected()) {

			if (serviceToConnect.equals("ML")) {
				new ConnectToAzureML().execute(GPSAppUtils.URLSTRING);
			} else {
				new ConnectToHSLApi().execute(GPSAppUtils.getHSLApiURL());
			}
		} else {
			Log.d(GPSAppUtils.GPS_ACTIVITY_TAG,
					"No network connection available.");
		}

	}

	/**
	 * Suorittaa HTTP-pyynnön POST-pyyntömenetelmän avulla, määriteltyyn
	 * osoitteeseen.
	 * 
	 */
	private class ConnectToAzureML extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... uri) {

			String url = GPSAppUtils.URLSTRING;

			SharedPreferences prefs = mAppContext.getSharedPreferences(
					GPSAppUtils.PREFS_TIME, 0);

			long restoredTime = prefs.getLong("proximityAlert", -1);

			Calendar currentTime = Calendar.getInstance();
			currentTime.setTimeInMillis(restoredTime);

			double hours = currentTime.get(Calendar.HOUR_OF_DAY);
			double minutes = currentTime.get(Calendar.MINUTE);

			double workStartedHours = (double) hours + (minutes / 60);

			HttpClient client = new DefaultHttpClient();

			HttpPost httpRequest = new HttpPost(url);

			String headerJsonString = GPSAppUtils
					.constructJSONString(workStartedHours);

			/*
			 * byte[] b = headerJsonString.getBytes(Charset.forName("UTF-8"));
			 * int contentLength = b.length;
			 */

			String ApiKey = "Bg4l+qqt05PaHDKOWzH+j6r6WEkNXnxYEMojzCVA2x+VgqzUSg5ZP04yVMgWAe80danbbCNf6dlWE7XdwCMV/g==";

			Log.d(GPSAppUtils.GPS_MANAGER_TAG, headerJsonString);

			String responseString = "";

			try {

				StringEntity entity = new StringEntity(headerJsonString,
						HTTP.UTF_8);
				entity.setContentType("application/json");

				httpRequest.setEntity(entity);

				httpRequest.setHeader("Authorization", "Bearer " + ApiKey);

				/*
				 * httpRequest.setHeader("Content-Length",
				 * entity.getContentLength() + "");
				 */
				httpRequest.setHeader("Content-Type",
						"application/json;charset=utf-8");
				// httpRequest.setHeader("Accept", "application/json");

				HttpResponse response = client.execute(httpRequest);

				StatusLine statusLine = response.getStatusLine();

				// Jos yhteys ok
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "Connection ok");
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					// Kirjoitetaan HTTP-pyynnön vastaus output streamiin
					response.getEntity().writeTo(out);

					// Suljetaan stream
					out.close();

					// Talletetaan muuttujaan output streamin sisältö stringina
					responseString = out.toString();

				} else {
					Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "Connection not ok");

					// Suljetaan yhteys
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}

			} catch (ClientProtocolException e) {

				Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, "Protocol");
				e.printStackTrace(System.out);
			} catch (IOException e) {
				Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, "ioexception");
				e.printStackTrace(System.out);
			}

			// Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, responseString);

			return responseString;
		}

		// Näyttää AsynTaskin tulokset
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "onPostExecute() was called");

			if (result != null) {
				Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, result);

				try {

					JSONArray arr = new JSONArray(result);
					float timeToAlarmVal = (float) arr.getDouble(4);

					SharedPreferences prefs = mAppContext.getSharedPreferences(
							GPSAppUtils.PREFS_TIME, 0);
					SharedPreferences.Editor prefEditor = prefs.edit();
					prefEditor.putFloat("timeToAlarm", timeToAlarmVal);
					prefEditor.commit();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else
				Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "Result was empty");
		}
	}

	/**
	 * Suorittaa HTTP-pyynnön POST-pyyntömenetelmän avulla, määriteltyyn
	 * osoitteeseen.
	 * 
	 */
	private class ConnectToHSLApi extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... uri) {

			HttpClient client = new DefaultHttpClient();

			HttpGet httpRequest = new HttpGet(uri[0]);

			String responseString = "";

			try {

				HttpResponse response = client.execute(httpRequest);

				StatusLine statusLine = response.getStatusLine();

				// Jos yhteys ok
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "Connection ok");
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					// Kirjoitetaan HTTP-pyynnön vastaus output streamiin
					response.getEntity().writeTo(out);

					// Suljetaan stream
					out.close();

					// Talletetaan muuttujaan output streamin sisältö stringina
					responseString = out.toString();

				} else {
					Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "Connection not ok");

					// Suljetaan yhteys
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}

			} catch (ClientProtocolException e) {

				Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, "Protocol");
				e.printStackTrace(System.out);
			} catch (IOException e) {
				Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, "ioexception");
				e.printStackTrace(System.out);
			}

			// Log.d(GPSAppUtils.LOCATION_RECEIVER_TAG, responseString);

			return responseString;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		// Näyttää AsynTaskin tulokset
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "onPostExecute() was called");

			if (result != null) {
				// Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, result);

				try {
					List<HSLAPIObject> objects = GPSAppUtils
							.getHSLAPIObjects(result);

					Gson gson = new Gson();
					String json = gson.toJson(objects);

					SharedPreferences prefs = mAppContext.getSharedPreferences(
							GPSAppUtils.PREFS_TIME, 0);
					SharedPreferences.Editor prefEditor = prefs.edit();
					prefEditor.putString("HSLObjectsList", json);
					prefEditor.commit();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else
				Log.d(GPSAppUtils.GPS_ACTIVITY_TAG, "Result was empty");
		}
	}
}
