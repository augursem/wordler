/**
 * 
 */
package com.augursolutions.wordler;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

/**
 * Abstract dictionary class
 * @author Steven Major
 *
 */
@SuppressWarnings("serial")
public abstract class Dictionary implements Serializable, Iterable<Word> {

	private transient Random rand;
	
	/**
	 * Gets the number of words in the dictionary
	 * @return number of words in the dictionary
	 */
	public abstract int getSize();
	
	/**
	 * Add a word to the {@link Dictionary} - should increment size by 1 if word is added
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
	
	/**
	 * Retrieve the {@link Word} object with associated with the {@code String value s}
	 * @param s spelling of the {@link Word} to return
	 * @return the {@link Word} object whose spelling is {@code s}. Returns {@code null} if the word is not in the dictionary
	 */
	public abstract Word getWord(String s);

	/**
	 * Sets the random seed for the {@link #getRandomWord()} and {@link #getRandomWord(Word[])} methods
	 * @param seed Any {@code int} is valid
	 */
	public void setSeed(int seed) {
		this.rand = new Random(seed);
	}
	
	protected Random getRand() {
		if(this.rand == null) {
			this.rand = new Random();
		}
		return this.rand;
	}
	
	/**
	 * Get a random {@link Word} from the dictionary - all words are equally likely to be retrieved regardless of size
	 * @return A randomly chosen {@link Word}
	 */
	public Word getRandomWord() {
		Random r = this.getRand();
		int index = r.nextInt(this.getSize());
		Iterator<Word> iter = this.iterator();
		Word w = iter.next();
		for(int i=0; i<index; i++) {
			w = iter.next();
		}
		return w;
	}
	
	/**
	 * Returns a randomly chosen member of the provided {@code lookupTable} array. Ideally,
	 * this is generated by calling {@link #getRandomWordLookupTable()} first and then passing
	 * the array from that call in to this method with each call. 
	 * @param lookupTable Array of {@link Word} objects
	 * @return a randomly chosen member of the provided {@code lookupTable} array
	 */
	public Word getRandomWord(Word[] lookupTable) {
		Random r = this.getRand();
		int index = r.nextInt(lookupTable.length);
		return lookupTable[index];
	}
	
	/**
	 * Generate an array with every Word in the Dictionary. This is not efficient from a memory
	 * stand-point but allows for fast generation of random words from the dictionary
	 * @return An array with every {@link Word} in the dictionary (in alphabetical order)
	 */
	public Word[] getRandomWordLookupTable() {
	  Word[] wordTable = new Word[this.getSize()];
	  int i=0;
	  for(Word w : this) {
		  wordTable[i++] = w;
	  }
	  return wordTable;
	}
}