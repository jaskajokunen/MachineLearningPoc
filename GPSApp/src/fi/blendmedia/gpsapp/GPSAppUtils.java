package fi.blendmedia.gpsapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Luokka sisältää helper metodeita ja konstantteja (tagit yms.)
 * 
 */
public class GPSAppUtils {

	// Luokkien tagit
	public static final String GPS_MANAGER_TAG = "GPSManager";
	public static final String LOCATION_RECEIVER_TAG = "LocationReceiver";
	public static final String GPS_ACTIVITY_TAG = "GPSFragment";
	public static final String UITASK_TAG = "UITask";

	// Intenttien actionit
	public static final String PROXIMITY_ACTION = "fi.blendmedia.gpsapp.PROXIMITY";
	public static final String ALARM_ACTION = "fi.blendmedia.gpsapp.NOTIFICATION_ALARM";

	// Intenttien keyt
	public static final String EVENT_ID_INTENT_EXTRA = "EventIDIntentExtraKey";
	public static final String TIMESTAMP_INTENT_EXTRA = "TimestampIntentExtraKey";

	// ID:t
	public static final int NOTIFICATION_ID = 9999;

	// Service kutsussa käytettävä string
	public static final String URLSTRING = "https://ussouthcentral.services.azureml.net/workspaces/234c1e5a4ffe485c8b5a9bd08d77ae96/services/a8813ef3d1b348a19c9da9df8494d830/score";

	// Tallennetut preferencet
	public static final String PREFS_TIME = "TimePrefsFile";

	public static String constructJSONString(double workEndedHours) {

		Gson gson = new Gson();
		String json;

		DateTime dt = new DateTime();
		int month = dt.getMonthOfYear();
		int dayOfWeek = dt.getDayOfWeek();

		String userName = "jkorhonen";

		ScoreRequest obj = new ScoreRequest(userName, Integer.toString(month),
				Integer.toString(dayOfWeek), String.valueOf(workEndedHours));

		json = gson.toJson(obj);

		return json;
	}

	/**
	 * Näytttää ilmoituksen
	 * 
	 * @param context
	 *            Context-olio
	 * @param message
	 *            Ilmoituksessa näytettävä viesti
	 * @param notification_id
	 *            Ilmotuksen tunniste
	 * @param locationString
	 *            Sijainnin sisältävä string
	 */
	public static void displayNotification(Context context, String message,
			int notification_id, String locationString) {

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setContentTitle("Proximity Alert!")
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentText(message + ": " + locationString);

		Intent resultIntent = new Intent(context, DestinationActivity.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setAutoCancel(true);

		String systemService = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(systemService);

		notificationManager.notify(notification_id, mBuilder.build());
	}

	/**
	 * Luo PendingIntentin, joka broadcastataan, kun tapahtuu
	 * sijaintipäivityksiä. Hyödyllinen, jos on useita palveluita, jotka
	 * vaativat sijaintipäivityksiä.
	 * 
	 * @param shouldCreate
	 *            Flag, joka kertoo, luodaanko PendingIntent. Jos arvo on true,
	 *            niin luodaan Pending Intent.
	 * @param mAppContext
	 *            Context-olio
	 * @return PendingIntent, joka toteuttaa broadcastin
	 */
	// PendingIntenttia käytetään usein paketoimaan Intentit, jotka laukaistaan
	// vastauksena tulevaisuuden eventille, kuten Notificationin painamiselle
	public static PendingIntent getLocationPendingIntent(boolean shouldCreate,
			Context mAppContext) {

		// Luodaan intent annetulla toiminnolla
		// Annettu toiminto on kustomoitu toiminto, koska halutaan tunnistaa
		// eventti sovelluksessa
		Intent broadcast = new Intent(PROXIMITY_ACTION);

		// Jos shouldCreate on true, niin asetetaan arvoksi 0, muutoin
		// FLAG_NO_CREATE
		// FLAG_NO_CREATE --> Jos PendingIntent ei ole olemassa, niin
		// palautetaan null, eikä luoda sitä
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;

		// getBroadcast() --> Haetaan PendingIntent, joka toteuttaa broadcastin
		// Parametrit:
		// Context, jossa PendingIntent toteuttaa broadcastin
		// Request code lähettäjälle
		// Intent, joka broadcastataan
		// flags
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
	}

	public static PendingIntent getAlarmPendingIntent(Context mAppContext) {

		Intent intentAlarm = new Intent(GPSAppUtils.ALARM_ACTION);
		return PendingIntent.getBroadcast(mAppContext, 0, intentAlarm, 0);
	}

	public static String getHSLApiURL() {
		double workLatitude = 60.165269;
		double workLongitude = 24.92948320000005;

		double destLatitude = 60.2571421;
		double destLongitude = 24.94543479999993;

		DateFormat df = new SimpleDateFormat("HH:mm");
		Calendar calobj = Calendar.getInstance();
		
		int hourPart = calobj.get(Calendar.HOUR_OF_DAY);
		int minPart = calobj.get(Calendar.MINUTE);
		String timeString = hourPart + "" + minPart;
		

		String HSLApiURL = "http://api.reittiopas.fi/hsl/prod/?request=route&user=hkiroutes&pass=ru654TE&epsg_in=4326&epsg_out=4326&show=1&from=%s,%s&to=%s,%s&change_cost=%s&wait_cost=%s&walk_cost=%s&transport_types=%s&time=%s";

		String waitCost = "1.0";
		String walkCost = "1.0";
		String changeCost = "0";
		String transportType = "1";
		String time = timeString;

		String formattedURL = String.format(HSLApiURL,
				Double.toString(workLongitude), Double.toString(workLatitude),
				Double.toString(destLongitude), Double.toString(destLatitude),
				changeCost, waitCost, walkCost, transportType, time);

		return formattedURL;
	}

	public static List<HSLAPIObject> getHSLAPIObjects(String result)
			throws JSONException {
		JSONArray newArray = new JSONArray(result);
		JSONArray innerJSONArray = newArray.getJSONArray(0);

		JSONObject rootJSONObject = (JSONObject) innerJSONArray.get(0);
		JSONArray legsJSONArray = rootJSONObject.getJSONArray("legs");

		String busNumber = "";
		String startAddress = "";
		String stopAddress = "";
		String arrTimeDate = "";
		String depTimeDate = "";

		List<HSLAPIObject> hslObjects = new ArrayList<HSLAPIObject>();

		for (int i = 0; i < legsJSONArray.length(); i++) {
			JSONObject obj = legsJSONArray.getJSONObject(i);

			if (obj.get("type").equals("1")) {
				busNumber = ((String) obj.get("code")).substring(2);
				busNumber = busNumber.substring(0, (busNumber.length() - 1));

				JSONArray locsJSONArray = obj.getJSONArray("locs");

				JSONObject startAddressObj = locsJSONArray.getJSONObject(0);
				startAddress = (String) startAddressObj.get("stopAddress");
				arrTimeDate = (String) startAddressObj.get("depTime");

				String formattedArrTimeDate = timeStringFormatter(arrTimeDate
						.substring(8));

				JSONObject stopAddressObj = locsJSONArray
						.getJSONObject((locsJSONArray.length() - 1));
				stopAddress = (String) stopAddressObj.get("stopAddress");
				depTimeDate = (String) stopAddressObj.get("depTime");

				String formattedDepTime = timeStringFormatter(depTimeDate
						.substring(8));

				HSLAPIObject hslObject = new HSLAPIObject(busNumber,
						startAddress, stopAddress, formattedArrTimeDate,
						formattedDepTime);
				hslObjects.add(hslObject);
			}
		}

		for (int i = 0; i < hslObjects.size(); i++) {
			System.out.println(hslObjects.get(i).getBusNumber());
			System.out.println(hslObjects.get(i).getStartAddress());
			System.out.println(hslObjects.get(i).getArrTimeDate());
			System.out.println(hslObjects.get(i).getStopAddress());
			System.out.println(hslObjects.get(i).getDepTimeDate());
			System.out.println();
		}
		return hslObjects;
	}

	private static String timeStringFormatter(String stringToFormat) {

		String hours = stringToFormat.substring(0, 2);
		String minutes = stringToFormat.substring(2);

		String formattedString = hours + ":" + minutes;

		return formattedString;
	}
	
	public static String construcAlarmString(List<HSLAPIObject> hslObjects) {
		
		String alarmString = "";
		
		for (int i = 0; i < hslObjects.size(); i++) {

			if (i != 0) {
				alarmString += "\n";
			}
			alarmString += hslObjects.get(i).getBusNumber();
			alarmString += "\n";
			alarmString += "Lähtöosoite: "
					+ hslObjects.get(i).getStartAddress();
			alarmString += "\n";
			alarmString += "Lähtöaika: "
					+ hslObjects.get(i).getArrTimeDate();
			alarmString += "\n";
			alarmString += "Pääteosoite: "
					+ hslObjects.get(i).getStopAddress();
			alarmString += "\n";
			alarmString += "Saapumisaika: "
					+ hslObjects.get(i).getDepTimeDate();
			alarmString += "\n";
		}
		
		return alarmString;
	}
	
	public static void setAlarm(float timeToAlarm, AlarmManager mAlarmManager, Context context) {
		
		int hours = (int) timeToAlarm;
		int minutes = (int) ((timeToAlarm - hours) * 60);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);
		
		long alarmTime = (cal.getTimeInMillis() - 1800000L);


		PendingIntent pIntentAlarm = GPSAppUtils
				.getAlarmPendingIntent(context);
		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				alarmTime, 15000, pIntentAlarm);
	}
	
	public static void cancelAlarm(Context context, AlarmManager mAlarmManager) {
		
		PendingIntent pIntentAlarm = GPSAppUtils
				.getAlarmPendingIntent(context);
		mAlarmManager.cancel(pIntentAlarm);

		Log.d(GPSAppUtils.GPS_MANAGER_TAG, "Hälytykset loppuivat!");
	}
	
	public static void clearPrefs(SharedPreferences prefs) {
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.clear();
		editor.commit();
	}
}
