package fi.blendmedia.gpsapp;

/**
 * Luokan avulla kerrotaan, onko asia (esim. Activity) näkyvissä.
 * 
 */
public class VisibilityManager {

	/**
	 * Näkyvyyden kertova flagi. Oletuksena false.
	 */
	private static boolean mIsVisible = false;

	/**
	 * Asettaa näkyvyyden kertovan flagin
	 * 
	 * @param visible
	 *            Onko asia näkyvissä
	 */
	public static void setIsVisible(boolean visible) {
		mIsVisible = visible;
	}

	/**
	 * Hakee näkyvyyden kertovan flagin
	 * 
	 * @return Flagin, joka kertoo, onko asia näkyvissä.
	 */
	public static boolean getIsVisible() {
		return mIsVisible;
	}

}
