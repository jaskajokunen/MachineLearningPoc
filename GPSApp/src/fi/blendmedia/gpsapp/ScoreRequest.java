package fi.blendmedia.gpsapp;

public class ScoreRequest {

	public String Id;
	public ScoreData Instance;
	
	public ScoreRequest(String userName, String monthOfYear, String dayofWeek, String workStartedHours) {
		Id = "score00001";
		Instance = new ScoreData(userName, monthOfYear, dayofWeek, workStartedHours);
	}
}
