package com.augursolutions.wordler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing details about a word - definitions, parts of speech, and spelling
 * @author Steven Major
 *
 */
public class Word implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Parts of speech:<br>
	 * {@link #ACRONYM} <br>
	 * {@link #ADJECTIVE} <br>
	 * {@link #ADVERB} <br>
	 * {@link #ARTICLE} <br>
	 * {@link #CONJUNCTION} <br>
	 * {@link #INTERJECTION} <br>
	 * {@link #NOUN} <br>
	 * {@link #PREPOSITION} <br>
	 * {@link #PRONOUN} <br>
	 * {@link #PROPER_NOUN} <br>
	 * {@link #VERB} <br>
	 * {@link #UNDEFINED}
	 */
	public enum Part_Of_Speech {
		/** An acronym, e.g. "SCUBA" */
		ACRONYM,
		/** An adjective, e.g. "verbose" */
		ADJECTIVE,
		/** An adverb (e.g. "quickly")*/
		ADVERB,
		/** An article (e.g. "hey")*/
		ARTICLE,
		/** A conjunction (e.g. "an")*/
		CONJUNCTION,
		/** An interjection (e.g. "hey")*/
		INTERJECTION,
		/** A noun (e.g. "word")*/
		NOUN,
		/** A preposition (e.g. "on")*/
		PREPOSITION,
		/** A pronoun (e.g. "them")*/
		PRONOUN,
		/** A Proper Noun */
		PROPER_NOUN,
		/** A verb (e.g. "write")*/
		VERB,
		/** No value provided or value was not recognized */
		UNDEFINED;
	}
	
	private String letters = null;
	private List<String> definitions = new ArrayList<>();
	private List<Part_Of_Speech> partsOfSpeech = new ArrayList<>();
	
	/**
	 * Empty constructor, this is not the same as {@code new Word(null, null, null)} - 
	 * this leaves {@code partsOfSpeech} as an empty list
	 */
	public Word(){}
	
	/**
	 * Initialize a Word object with no definitions or parts of speech
	 * @param letters value to assign to the {@code letters field}
	 */
	public Word(String letters) {
		this(letters,null,null);
	}
	
	/**
	 * Initialize a Word object with spelling and definition, but no parts of speech
	 * @param letters value to assign to the {@code letters field}
	 * @param definition value to add to the {@code definition list} 
	 */
	public Word(String letters, String definition) {
		this(letters,null,null);
		this.definitions.add(definition);
	}
	
	/**
	 * General constructor for Word object
	 * @param letters Spelling of the word
	 * @param definitions List of definitions - more than one for homographs (words with the same spelling but different meanings)
	 * @param partsOfSpeech parts of speech for each definition
	 */
	public Word(String letters, List<String> definitions, List<Part_Of_Speech> partsOfSpeech) {
		this.letters = letters.toUpperCase();
		if(definitions != null)
		  this.definitions = definitions;
		if(partsOfSpeech == null)
			this.partsOfSpeech.add(Part_Of_Speech.UNDEFINED);
		else
			this.partsOfSpeech = partsOfSpeech;
	}
	
	/**
	 * Get letters of the word as a String
	 * @return letters field (can be null)
	 */
	public String getLetters() {
		return this.letters;
	}
	/**
	 * Set the letters field
	 * @param letters Letters that make up the word, i.e. its spelling
	 */
	public void setLetters(String letters) {
		this.letters = letters;
	}

	/**
	 * Get the definition of the word
	 * @return the definition field (can be null)
	 */
	public List<String> getDefinitions() {
		return this.definitions;
	}
	/**
	 * Set the definition of the word
	 * @param definitions A string that defines the word
	 */
	public void setDefinitions(List<String> definitions) {
		this.definitions = definitions;
	}
	/**
	 * Check that a word has a definition
	 * @return {@code true} if the {@code definition} field is not null and not empty
	 */
	public boolean hasDefintiions( ) {
		return this.definitions != null && !this.definitions.isEmpty();
	}
	
	/**
	 * List of parts of speech associated with the word - one per definition
	 * @return List of {@link Part_Of_Speech} values for this word
	 */
	public List<Part_Of_Speech> getPartsOfSpeech() {
		return this.partsOfSpeech;
	}
	/**
	 * Assign a list of {@link Part_Of_Speech} values to a word.
	 * @param partsOfSpeech List of {@link Part_Of_Speech} values to associate with the word
	 */
	public void setPartsOfSpeech(List<Part_Of_Speech> partsOfSpeech) {
		this.partsOfSpeech = partsOfSpeech;
	}
	
	/**
	 * Returns the {@code letters} field as a char array
	 * @return The {@code letters} field as a char array
	 */
	public char[] toChars() {
		if(this.letters == null || this.letters.isEmpty())
			return new char[0];
		char[] chars = new char[this.letters.length()];
		this.letters.getChars(0, this.letters.length(), chars, 0);
		return chars;
	}
	
	/**
	 * Returns the length of the letters field - identical to {@code getLetters().length()} when {@code letters} is not null 
	 * @return The length of the letters field
	 */
	public int length() {
		if(this.letters == null)
			return 0;
		return this.letters.length();
	}
	
	/**
	 * Returns the letters field, or "" if thatfield is null
	 */
	public String toString() {
		return this.getLetters() == null ? "" : this.getLetters();
	}
}