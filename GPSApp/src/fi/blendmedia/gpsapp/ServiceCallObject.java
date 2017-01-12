package fi.blendmedia.gpsapp;


/**
 * Olio, joka sis‰lt‰‰ service kutsussa vaadittavat arvot.
 *
 */
public class ServiceCallObject {

	String UserName;
	int MonthOfYearNumber;
	int DayofWeekNumber;
	double WorkStartedHours;
	
	
	public ServiceCallObject() {
		
	}
	
	public ServiceCallObject(String userName, int monthOfYearNumber, int dayOfWeekNumber, double workStartedHours) {
		UserName = userName;
		MonthOfYearNumber = monthOfYearNumber;
		DayofWeekNumber = dayOfWeekNumber;
		WorkStartedHours = workStartedHours;
	}

}
