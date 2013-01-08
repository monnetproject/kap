package eu.monnetproject.l10n;

import java.net.URI;
import java.util.List;

import eu.monnetproject.lang.Language;

/**
 * A lexicon that is adapted for localization of a given application
 */
public interface LocalizationLexicon {

	/** Get the default form for a concept
	 * @param sense The URI identifying the concept
	 * @param language The target language
	 * @return A priority-ordered list of string
	 */
	List<String> form(URI sense, Language language);

	/**
 	 * Get a (longer) description for a concept
 	 * @param sense The URI identifying the concept
	 * @param language The target language
	 * @return A priority-ordered list of string
	 */
	List<String> description(URI sense, Language language);

	/**
	 * Resolve a particular keyed localization string
	 * @param key The representation of the key
	 * @param language The target language
	 * @return The best result
	 */
	String get(String key, Language language);
	
	/**
	 * Resolve a particular keyed localization string with parameters
	 * @param key The representation of the key
	 * @param arguments The list of arguments
	 * @param language The target language
	 * @return The best result
	 */
	String get(String key, List<Object> arguments, Language language);
        
        /**
         * Get the app key. Used to distinguish different localization lexica for
         * different applications
         * @returns The app key
         */
        String getAppKey();
}
