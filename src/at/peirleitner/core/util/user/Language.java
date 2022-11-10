package at.peirleitner.core.util.user;

/**
 * This class represents a language that can be selected by a {@link User}.<br>
 * This does not differ between multiple language choices (ex.: EN_US and EN_GB
 * will both be named EN).<br><br>
 * More languages will be added once a good percentage of files have been translated into it on crowdin.
 * 
 * @return Language
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public enum Language {

	/**
	 * English language
	 * 
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	ENGLISH("English", "English", "EN", "eng"),
	GERMAN("Deutsch", "German", "DE", "deu");

	private final String nativeName;
	private final String englishName;
	private final String code;
	private final String isoCode;

	private Language(String nativeName, String englishName, String code, String isoCode) {
		this.nativeName = nativeName;
		this.englishName = englishName;
		this.code = code;
		this.isoCode = isoCode;
	}

	/**
	 * 
	 * @return Native name of the language, written as by native speakers.
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getNativeName() {
		return nativeName;
	}

	/**
	 * 
	 * @return {@link #getNativeName()} translated into {@link #ENGLISH}.
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getEnglishName() {
		return englishName;
	}

	/**
	 * 
	 * @return Code of the language
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getCode() {
		return code;
	}
	
	/**
	 * 
	 * @return ISO 639-3 Code
	 * @since 1.0.11
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getIsoCode() {
		return isoCode;
	}

}
