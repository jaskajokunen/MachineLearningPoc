package fi.blendmedia.gpsapp;

/**
 * Luokan avulla kerrotaan, onko asia (esim. Activity) n�kyviss�.
 * 
 */
public class VisibilityManager {

	/**
	 * N�kyvyyden kertova flagi. Oletuksena false.
	 */
	private static boolean mIsVisible = false;

	/**
	 * Asettaa n�kyvyyden kertovan flagin
	 * 
	 * @param visible
	 *            Onko asia n�kyviss�
	 */
	public static void setIsVisible(boolean visible) {
		mIsVisible = visible;
	}

	/**
	 * Hakee n�kyvyyden kertovan flagin
	 * 
	 * @return Flagin, joka kertoo, onko asia n�kyviss�.
	 */
	public static boolean getIsVisible() {
		return mIsVisible;
	}

}
