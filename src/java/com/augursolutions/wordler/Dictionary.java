/**
 * 
 */
package com.augursolutions.wordler;

import java.io.Serializable;

/**
 * Abstract dictionary class
 * @author Steven Major
 *
 */
@SuppressWarnings("serial")
public abstract class Dictionary implements Serializable{

	/**
	 * Gets the number of words in the dictionary
	 * @return number of words in the dictionary
	 */
	public abstract int getSize();
	
	/**
	 * Add a word to the {@linkDictionary} - should increment size by 1 if word is added
	 * @param word Word to be added to the {@link Dictionary}
	 * @return {@code true} if the Word is added ({@code false} if the word already exists or if {@code word} is null or otherwise invalid)
	 */
	public abstract boolean add(String word);

	/**
	 * Add all elements of a set to the dictionary 
	 * @param <T> any class that implements Iterator&lt;String&gt;
	 * @param words words to add to the dictionary
	 */
	public <T extends Iterable<String>> void addAll(T words) {
		if(words == null)
			return;
		for(String s : words) {
			this.add(s);
		}
	}
	
	/**
	 * Remove a word from the {@link Dictionary}; the {@link Dictionary} object is 
	 * modified in the process. If {@code word} is not present in the dictionary, it should be
	 * left unmodified - should increment size by 1 if word is removed
	 * @param word Word to be removed from the {@link Dictionary}
	 */
	public abstract void remove(String word);
	
	/**
	 * Determine if a word is in the dictionary
	 * @param word String to check against the dictionary
	 * @return {@code true} if the word is in the dictionary
	 */
	public abstract boolean contains(String word);
}
