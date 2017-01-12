package fi.blendmedia.gpsapp;

import java.util.HashMap;

public class ScoreData {

	private HashMap<String, String> FeatureVector;
	private HashMap<String, String> GlobalParameters;

	public ScoreData(String userNameVal, String monthOfYearVal,
			String dayOfWeekVal, String workStartedVal) {
		FeatureVector = new HashMap<String, String>();
		FeatureVector.put("UserName", userNameVal);
		FeatureVector.put("MonthofYearNumber", monthOfYearVal);
		FeatureVector.put("DayofWeekNumber", dayOfWeekVal);
		FeatureVector.put("WorkStartedHours", workStartedVal);

		GlobalParameters = new HashMap<String, String>();

	}
}
