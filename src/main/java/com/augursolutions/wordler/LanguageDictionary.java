package com.augursolutions.wordler;

import java.util.Iterator;

/**
 * Interface that describes additional methods for dictionary classes that will hold words
 * as Word objects; i.e. contain definitions and parts of speech for each word
 * @author Steven Major
 *
 */
public interface LanguageDictionary  {

	/**
	 * Retrieve the {@link Word} object with associated with the {@code String value s}
	 * @param s spelling of the {@link Word} to return
	 * @return the {@link Word} object whose spelling is {@code s}. Returns {@code null} if the word is not in the dictionary
	 */
	public abstract Word getWord(String s);
	
	/**
	 * Add a new {@link Word} object to the dictionary
	 * @see {@link Dictionary#add(String)}
	 */
	public abstract boolean add(Word word);

	/**
	 * LanguageDictionary classes need to allow for iterating on {@link Word} objects.
	 */
	public abstract Iterator<Word> wordIterator();

}
