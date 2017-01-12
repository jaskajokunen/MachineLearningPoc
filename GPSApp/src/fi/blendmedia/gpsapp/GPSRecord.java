package fi.blendmedia.gpsapp;

import java.util.Date;
import java.util.Locale;

/**
 * Esitt‰‰ yht‰ sovelluksen ajokertaa
 * 
 * 
 */
public class GPSRecord {

	/**
	 * Muuttuja, joka esitt‰‰ nykyist‰ aikaa.
	 */
	private Date mStartDate;

	/**
	 * Konstruktori, joka asettaa ajaksi nykyisen ajan.
	 */
	public GPSRecord() {
		mStartDate = new Date();
	}

	/**
	 * Palauttaa nykyist‰ aikaa esitt‰v‰n muuttujan.
	 * @return Nykyist‰ aikaa esitt‰v‰ muuttuja.
	 */
	public Date getStartDate() {
		return mStartDate;
	}

	/**
	 * Asettaa aloitusajaksi parametrina annetun ajan.
	 * @param startDate Aloitusaikaa esitt‰v‰ muuttuja.
	 */
	public void setStartDate(Date startDate) {
		mStartDate = startDate;
	}

	/**
	 * 
	 * @param endMillisecs Sovelluksen ajokerran t‰m‰nhetkinen pituus.
	 * @return Kuinka pitk‰‰n sovellusta on ajettu sekunneissa.
	 */
	public int getDurationSeconds(long endMillisecs) {
		return (int) ((endMillisecs - mStartDate.getTime()) / 1000);
	}

	/**
	 * Muotoilee parametrina annetun ajan string-muotoon.
	 * @param durationSeconds Kuinka pitk‰‰n sovellusta on ajettu sekunneissa.
	 * @return Palauttaa muotoillun stringin.
	 */
	public static String formatDuration(int durationSeconds) {
		int seconds = durationSeconds % 60;
		int minutes = ((durationSeconds - seconds) / 60) % 60;
		int hours = (durationSeconds - (minutes * 60) - seconds) / 3600;
		return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours,
				minutes, seconds);
	}

}
