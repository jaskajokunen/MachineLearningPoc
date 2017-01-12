package fi.blendmedia.gpsapp;

/**
 * Esitt‰‰ proximity alertissa asetettua paikkaa (geofencea).
 *
 */
public class GeofenceObject {
	
	private double latitude;
	private double longitude;
	private int id;
	
	public GeofenceObject(double lat, double lon,int ID) {
		latitude = lat;
		longitude = lon;
		id = ID;
	}
	
	public int getID() {
		return id;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}

}
