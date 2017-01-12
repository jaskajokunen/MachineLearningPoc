package fi.blendmedia.gpsapp;

public class HSLAPIObject {

	private String busNumber;
	private String startAddress;
	private String stopAddress;
	private String arrTimeDate;
	private String depTimeDate;

	public HSLAPIObject(String bNumber, String startAdd, String stopAdd,
			String arrTime, String depTime) {
		busNumber = bNumber;
		startAddress = startAdd;
		stopAddress = stopAdd;
		arrTimeDate = arrTime;
		depTimeDate = depTime;
	}

	public String getBusNumber() {
		return busNumber;
	}

	public void setBusNumber(String busNumber) {
		this.busNumber = busNumber;
	}

	public String getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}

	public String getStopAddress() {
		return stopAddress;
	}

	public void setStopAddress(String stopAddress) {
		this.stopAddress = stopAddress;
	}

	public String getArrTimeDate() {
		return arrTimeDate;
	}

	public void setArrTimeDate(String arrTimeDate) {
		this.arrTimeDate = arrTimeDate;
	}

	public String getDepTimeDate() {
		return depTimeDate;
	}

	public void setDepTimeDate(String depTimeDate) {
		this.depTimeDate = depTimeDate;
	}
}
