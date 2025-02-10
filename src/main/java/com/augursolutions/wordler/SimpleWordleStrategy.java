package com.augursolutions.wordler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Most basic strategy for guessing words. First word is fixed (optional implementation allows for 
 * passing in a first guess to aid in testing). After each guess, all words that do not match the
 * response from the guess are removed (reducedDictionary object keeps track of possible words).
 * Next guess is the first (alphabetically) word in the remaining dictionary.
 * @author Steven Major
 *
 */
public class SimpleWordleStrategy implements WordleStrategy {

	public static void main(String[] args) {
		TreeSetDictionary wordleDictionary = new TreeSetDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		WordleEngine engine = new WordleEngine(wordleDictionary);
		SimpleWordleStrategy strategy = new SimpleWordleStrategy(wordleDictionary);
		String solution = "LEVEL";
		engine.newPuzzle(solution);
		String guess = strategy.firstGuess("GEESE");
		engine.guessWord(guess);
		while(!engine.isSolved() && engine.getGuessCount() < 6) {
			guess = strategy.nextGuess(guess, engine.getResponse(engine.getGuessCount()));
			engine.guessWord(guess);
		}
		System.out.println("For puzzle '" + solution + "': after " + engine.getGuessCount() + " guesses, final guess was: " + guess);
	}
	
	private Dictionary dictionary;
	private Dictionary reducedDictionary;
	
	public SimpleWordleStrategy(Dictionary d) {
		this.dictionary = d;
	}
	
	/**
	 * Initializes the {@code reducedDictionary} object and guesses "GRAND"
	 * (which is, in my humble opinion, a solid first guess)
	 */
	public String firstGuess() {
		this.reducedDictionary = (Dictionary)this.dictionary.clone();
		return "GRAND";
	}
	
	/**
	 * For testing purposes only
	 */
	public String firstGuess(String guess) {
		firstGuess();
		return guess;
	}
	
	/**
	 * Remove all words from {@code reducedDictionary} that do not match the information in
	 * the response. Next guess is the first word in updated {@code reducedDictionary}
	 * @see {@link #updateReducedDictionary(String, WordleEngine.Response[])}
	 */
	public String nextGuess(String previousGuess, WordleEngine.Response[] previousResponse) {
		updateReducedDictionary(previousGuess, previousResponse);
		for( String w : this.reducedDictionary) {
			return w;
		}
		// If we get here, the dictionary is now empty - things have gone wrong. Just guess "GRAND" again.
		return "GRAND";
	}
	
	public Dictionary getReducedDictionary() {
		return this.reducedDictionary;
	}
	
	/**
	 * Given a guess and response, removes all words from the dictionary that do not meet the criteria.
	 * Assumes that guess and response are of length 5;
	 * @param guess
	 * @param response
	 */
	public void updateReducedDictionary(String guess, WordleEngine.Response[] response ) {
		// Letters that are gray and that have no corresponding yellow or green entry (If you guess 
		// a word with 2 L's one can be yellow and the other gray).
		List<Character> grayLetters = new ArrayList<>();
		// Letters that were green
		List<Integer> greenPositions = new ArrayList<>();
		List<Character> greenLetters = new ArrayList<>();
		//Letters that are yellow
		List<Integer> yellowPositions = new ArrayList<>();
		List<Character> yellowLetters = new ArrayList<>();
		// Letters that are gray, but where the same letter was yellow or green elsewhere
		List<Integer> grayishPositions = new ArrayList<>();
		List<Character> grayishLetters = new ArrayList<>();
		
		// Populate Green and Yellow lists
		List<Integer> grayIndexes = new ArrayList<>();
		for(int i=0; i<5; i++) {
			if(response[i] == WordleEngine.Response.GREEN) {
				greenPositions.add(i);
				greenLetters.add(guess.charAt(i));
				continue;
			}
			if(response[i] == WordleEngine.Response.YELLOW) {
				yellowPositions.add(i);
				yellowLetters.add(guess.charAt(i));
				continue;
			}
			// This is gray or grayish
			grayIndexes.add(i);
		}
		
		// Populate Gray and Grayish lists
		for(int i : grayIndexes) {
			if(greenLetters.contains(guess.charAt(i)) || yellowLetters.contains(guess.charAt(i))) {
				grayishPositions.add(i);
				grayishLetters.add(guess.charAt(i));
			} else {
				grayLetters.add(guess.charAt(i));
			}
		}
		
		// Now filter out words that don't match the response
		for(String w : dictionary) {
			boolean done = false;
			// Word should have green letters in exact position
			for(int i=0; i<greenPositions.size(); i++) {
				char greenLetter = greenLetters.get(i);
				int greenPosition = greenPositions.get(i);
				if(w.charAt(greenPosition) != greenLetter) {
					this.reducedDictionary.remove(w);
					done = true;
					break;
				}
			}
			if(done) continue; // Once a word is removed, we don't need to keep checking
			
			// Word can't contain any gray letters (grayish is more complicated, hence the "ish")
			for(char c : grayLetters) {
				if(w.contains(String.valueOf(c))) {
					this.reducedDictionary.remove(w);
					done = true;
					break;
				}
			}
			if(done) continue; // Once a word is removed, we don't need to keep checking
			
			// Grayish letters can't appear in the word in the same position they appeared in the guess
			for(int i=0; i<grayishPositions.size(); i++) {
				char grayishLetter = grayishLetters.get(i);
				int grayishPosition = grayishPositions.get(i);
				if(w.charAt(grayishPosition) == grayishLetter) {
					this.reducedDictionary.remove(w);
					done = true;
					break;
				}
			}
			if(done) continue; // Once a word is removed, we don't need to keep checking
			
			// Yellow letters must be in the word, but not in the same position as in the guess
			for(int i=0; i<yellowPositions.size(); i++) {
				char yellowLetter = yellowLetters.get(i);
				int yellowPosition = yellowPositions.get(i);
				if(w.charAt(yellowPosition) == yellowLetter || !w.contains(String.valueOf(yellowLetter))) {
					this.reducedDictionary.remove(w);
					done = true;
					break;
				}
			}
			if(done) continue; // Once a word is removed, we don't need to keep checking
			
			// Finally, cardinality check. Grayish letters are letters where we know the exact cardinality
			// of the letter in the solution. For example, if the solution is "LEVEL" and the guess was
			// "GEESE", then the response was "GRAY | GREEN | YELLOW | GRAY | GRAY". The last GRAY, corresponding
			// to the last E in "GEESE" is "grayish" and tells us that there are exactly 2 E's in the solution;
			// the one that is in the green list and the one that is in the yellow list
			Set<Character> distinctGrayish = new HashSet<>();
			for(char c : grayishLetters) { 
				distinctGrayish.add(c);
			}
			for(char letter : distinctGrayish) {
				if(letterCountInWord(w, letter) != letterCountFromResponse(greenLetters, yellowLetters, letter)) {
					this.reducedDictionary.remove(w);
					break;
				}
			}
		}
	}
	
	/**
	 * @param word Word to check
	 * @param letter Single character
	 * @return The number of times {@code letter} appears in {@link word}
	 */
	public int letterCountInWord(String word, char letter) {
		int count = 0;
		for(char c : word.toCharArray()) {
			if(letter == c) count++;
		}
		return count;
	}
	
	/**
	 * @param greenLetters List of green letters from the response
	 * @param yellowLetters List of yellow letters from the response
	 * @param letter letter to check
	 * @return number of times letter appears in greenLetters and yellowLetters combined
	 */
	public int letterCountFromResponse(List<Character> greenLetters, List<Character> yellowLetters, char letter) {
		int count = 0;
		for(char c : greenLetters) {
			if(letter == c) count++;
		}
		for(char c : yellowLetters) {
			if(letter == c) count++;
		}
		return count;
	}
	
}
