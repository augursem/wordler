package com.augursolutions.wordler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.augursolutions.wordler.Dictionary.DictionaryNode;

/**
 * 
 * Used with the Dictionary.filter method to remove words
 * from a dictionary based on certain rules
 * <p>
 *  <b>requiredLetters:</b> Letters that must be in the word. If a word
 *    does not contain ALL letters in the string, it is removed.
 *    TODO: Duplicates should be allowed / handled
 *  <br><br>
 * <b>possibleLetters:</b> Letters that, together with requiredLetters,
 *   define a reduced alphabet that all words must use. For example,
 *   if possibleLetters are "BOARD", then ARBOR, BOAR, BOARD, ROAD, ABOARD, 
 *   and DOOR are all possible words, but BORAX is not. Duplicate letters
 *   are ignored; "BOARD" and "BBOOOOAAARRRRD" are equivalent.
 *   TODO: Issue warning and trim list for duplicates
 *  <br><br>
 * <b>wordSizeMin:</b> Minimum number of letters that a word must contain. 
 *   e.g. if wordSizeMin is 5, all words with 1,2,3 and 4 letters are removed
 *  <br><br>
 * <b>wordSizeMax:</b> Maximum number of letters that a word must contain. 
 *   e.g. if wordSizeMax is 5, all words with 6 letters or more are removed
 * </p>
 * @author Steven Major
 * @since 1.0
 */
public class DictionaryFilter {
	private Set<Character> requiredLetters = new HashSet<>();
	private Set<Character> possibleLetters = new HashSet<>();
	private Integer wordSizeMin = null;
	private Integer wordSizeMax = null;
	
	public boolean hasRequiredLetters() {
	  return this.requiredLetters != null && !this.requiredLetters.isEmpty();	
	}
	
	public Set<Character> getRequiredLetters() {
		return this.requiredLetters;
	}
	
	public void addRequiredLetters(String letters) {
		for(int i=0; i<letters.length(); i++) {
			this.requiredLetters.add(letters.charAt(i));
		}
	}
	
	public void addRequiredLetter(Character c) {
		this.requiredLetters.add(c);
	}
	
	public boolean hasPossibleLetters() {
	  return this.possibleLetters != null && !this.possibleLetters.isEmpty();	
	}
	
	public Set<Character> getPossibleLetters() {
		return this.possibleLetters;
	}
	
	// If any letters are required, then they are also 
	// possible letters
	public Set<Character> getAllPossibleLetters() {
		Set<Character> allPossibleLetters = new HashSet<>();
		allPossibleLetters.addAll(this.possibleLetters);
		allPossibleLetters.addAll(this.requiredLetters);
		return allPossibleLetters;
	}
	
	public void addPossibleLetters(String letters) {
		for(int i=0; i<letters.length(); i++) {
			this.possibleLetters.add(letters.charAt(i));
		}
	}
	
	public void addPossibleLetter(Character c) {
		this.possibleLetters.add(c);
	}
	
	public boolean hasWordSizeMin() {
		return this.wordSizeMin != null && this.wordSizeMin > 0;
	}
	
	public Integer getWordSizeMin() {
		return this.wordSizeMin;
	}
	
	public void setWordSizeMin(Integer i) {
		if(this.hasWordSizeMax() && i > this.wordSizeMax) {
		  System.out.println("ERROR: Filter has a word size max of " + this.wordSizeMax + "; can't have wordSizeMin > wordSizeMax. No wordSizeMin will be set.");
		  return;
		}	
		this.wordSizeMin = i;
	}
	
	public boolean hasWordSizeMax() {
		return this.wordSizeMax != null && this.wordSizeMax > 0;
	}
	
	public Integer getWordSizeMax() {
		return this.wordSizeMax;
	}
	
	public void setWordSizeMax(Integer i) {
		if(this.hasWordSizeMin() && i < this.wordSizeMin) {
			System.out.println("ERROR: Filter has a word size min of " + this.wordSizeMin + " - can't have wordSizeMax < wordSizeMin. No wordSizeMax will be set.");
			return;
		}	
		this.wordSizeMax = i;
	}
	
	/**
	 * Takes a {@link Filter} object and returns a {@link Dictionary} object
	 * which only contains words that meet the {@link Filter} criteria
	 * 
	 * @param filter A {@link Filter} object to apply to this {@link Dictionary}
	 * @return A {@link Dictionary} object with only words that meet the {@link Filter}
	 *         criteria
	 * @since 1.0
	 */
	public Dictionary applyTo(Dictionary dictionary) {
		Dictionary filteredDictionary = new Dictionary();
		applyTo(dictionary.getRootNode(), filteredDictionary);
		return filteredDictionary;
	}
	
	/**
	 * Recursive implementation of the {@link #applyFilter(Filter)} method
	 * used internally in recursion
	 * @param filter - The {@link Filter} being applied
	 * @param node - current node in the {@link Dictionary} tree
	 * @param filteredDictionary - The filtered {@link Dictionary} object that is being constructed
	 */
	private void applyTo(DictionaryNode node, Dictionary filteredDictionary)
	{
		if(node == null || node.getChildren() == null || node.getChildren().isEmpty())
			return;

		// Get letters so far
		String wordFragment = node.getWordFragment();
		
		// Apply wordSizeMax:
		// If we have more letters than wordSizeMax, stop searching along this branch
		if(this.hasWordSizeMax() && wordFragment.length() > this.getWordSizeMax()) {
			return;
		}
		
		for (Map.Entry<Character, DictionaryNode> entry : node.getChildren().entrySet()) {
    		DictionaryNode currentChildNode = entry.getValue();
    		
    		// WORD_ENDING node
	    	if(currentChildNode.getLetter() == DictionaryNode.WORD_ENDING) {
	    		// Apply wordSizeMin:
	    		// Only add word if it meets wordSizeMin requirement
	    		if(!this.hasWordSizeMin() || wordFragment.length() >= this.getWordSizeMin()) {
		    		boolean addWord = true;
		    		// Apply requiredLetters
		    		// If any required letter is missing, don't add the word
		    		if(this.hasRequiredLetters()) {
		    			addWord = wordContainsRequiredLetters(wordFragment, this.getRequiredLetters());
		    		}
		    		if(addWord)
		    			filteredDictionary.addWord(wordFragment);
	    		}
	    	}
	    	// Regular character node
	    	else {
	    		// Apply possibleLetters
				// If we find a letter that is not in the possibles list, stop searching
				// along this branch, i.e. only continue on a branch if this letter is in the
	    		// possibleLetters list
	    		if(!this.hasPossibleLetters() || this.getAllPossibleLetters().contains(currentChildNode.getLetter())) {
	    		  applyTo(currentChildNode, filteredDictionary);
	    		}
	    	}
	    }
	}
	
	/**
	 * Check if all Characters in {@code requiredLetters} are in {@Code word}. Word can have other 
	 * characters. For example, if requriedLetters = {'A','C'}, then "CAT" and "ATTACK" would return 
	 * {@code true}, while "AND" and "THEN" would return {@code false}
	 * @param word Word to check for presence of all {@code requiredLetters}
	 * @param requiredLetters Characters that must be in {@code word} in order for the result to be {@code true}
	 * @return {@code true} if each Character in {@code requiredLetters} is in {@code word}
	 */
	private boolean wordContainsRequiredLetters(String word, Set<Character> requiredLetters) {
		Iterator<Character> requiredChars = requiredLetters.iterator();
		while(requiredChars.hasNext()) {
			if(word.indexOf(requiredChars.next()) < 0) {
				return false;
			}
		}
		return true;
	}
}