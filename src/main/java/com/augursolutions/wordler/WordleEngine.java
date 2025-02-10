package com.augursolutions.wordler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class that performs the basic functionality needed to play Wordle:
 * <ul>
 * <li>stores a dictionary of valid words</li>
 * <li>allows you to set a puzzle solution</li>
 * <li>takes word guesses and checks them against the dictionary and then returns a response
 *    based on how the guess compares to the solution</li>
 * <li>tracks guesses and responses for a given puzzle</li>
 * <p>
 * <b>Sample Usage:</b>
 *  <pre>
 *  TreeMapLanguageDictionary wordleDictionary = new TreeMapLanguageDictionary();
 *  DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
 *  WordleEngine wordleEngine = new WordleEngine(wordleDictionary);
 *  wordleEngine.newPuzzle("FIRST");
 *  wordleEngine.guessWord("WRONG");
 *  boolean solved = wordleEngine.isSolved();
 *  </pre>
 *  
 * @author Steven Major
 *
 */
public class WordleEngine {
	
	private Dictionary dictionary;
	private Puzzle puzzle;

	private static final Logger LOGGER = Logger.getLogger( WordleEngine.class.getName() );
	
	public <T extends Dictionary > WordleEngine(T dictionary) {
		this.dictionary = dictionary;
		if(dictionary == null || dictionary.getSize() == 0) {
			LOGGER.severe("Dictionary used to initialize the WordleEngine can not be null or empty.");
		}
		for(String w : dictionary) {
			if(w.length() != 5) {
				dictionary.remove(w.toString());
			}
		}
		if(dictionary.getSize() == 0) {
			LOGGER.severe("Dictionary has no 5-letter words to use.");
		}
	}
	
	/**
	 * Creates a new Wordle puzzle to solve - any information about guesses from
	 * a previous puzzle are lost. At most six guesses can be made for a given puzzle.
	 * @param solution Five letter word, must be in the dictionary used to initialize the {@link WordleEngine}
	 * @param hardMode <b>NOT YET IMPLEMENTED</b> Whether or not rules for hard mode are applied to guesses
	 * @return true if the guess was valid and was added to the list of guesses (and therefore has a response associated with it)
	 */
	public boolean newPuzzle(String solution, boolean hardMode) {
		if(solution == null ) {
			LOGGER.severe("Unable to initialize puzzle with null solution");
			this.puzzle = null;
			return false;
		}
		if(solution.length() != 5) {
			LOGGER.severe("Unable to initialize puzzle with solution '" + solution + "' - it is not a 5 letter word.");
			this.puzzle = null;
			return false;
		}
		if(!this.dictionary.contains(solution)) {
			LOGGER.severe("Unable to initialize puzzle with solution '" + solution + "' - it is not in the dictionary.");
			this.puzzle = null;
			return false;
		}
		
		this.puzzle = new Puzzle(solution, hardMode);
		return true;
	}
	
	/**
	 * Wrapper for {@link WordleEngine#newPuzzle(String,boolean)} that sets hardMode to false
	 * @see @link{WordleEngine#newPuzzle(String,boolean)}
	 */
	public boolean newPuzzle(String solution) {
		return newPuzzle(solution,false);
	}
	
	/**
	 * Applies a guess to the current puzzle. Number of guesses, guess history, and response history are updated
	 * accordingly. Response for a guest is retrieved by calling {@link WordleEngine#getResponse(int)}. Guess must 
	 * be 5 letters long and must be in the dictionary.
	 * @param guess Guess to apply to the current puzzle
	 * @return true if the guess was a valid guess.
	 */
	public boolean guessWord(String guess) {
		guess = guess.toUpperCase();
		// Only get 6 guesses
		if(this.getGuessCount() > 5) {
			LOGGER.warning("Puzzle already has 6 guesses - no additional guesses are allowed");
			return false;
		}
		// Make sure word is in the original dictionary
		if(guess.length() != 5 || !this.dictionary.contains(guess)) {
			return false;
		}
		// First, initialize response values as all GRAY
		for(int i=0; i<5; i++) {
			this.puzzle.responses[this.puzzle.guessCount][i] = Response.GRAY;
		}
		// Now find 'GREEN' letters - guess letter matches response letter perfectly
		// Any letters not assigned as 'GREEN' need to be checked next to see if they
		// should be 'YELLOW'
		List<Character> remainingGuessLetters = new ArrayList<>();
		List<Character> remainingSolutionLetters = new ArrayList<>();
		List<Integer> remainingLetterIndices = new ArrayList<>();
		for(int i=0; i<5; i++) {
			if(guess.charAt(i) == this.puzzle.solution.charAt(i)) {
				this.puzzle.responses[this.puzzle.guessCount][i] = Response.GREEN;
			} else {
				remainingGuessLetters.add(guess.charAt(i));
				remainingSolutionLetters.add(this.puzzle.solution.charAt(i));
				remainingLetterIndices.add(i);
			}
		}
		if(remainingGuessLetters.isEmpty()) {
			this.puzzle.solved = true;
		}
		
		// Go through remaining letters and check for any YELLOW letter
		for(int i=0; i<remainingGuessLetters.size(); i++) {
			Character c = remainingGuessLetters.get(i);
			if(remainingSolutionLetters.contains(c)) {
				this.puzzle.responses[this.puzzle.guessCount][remainingLetterIndices.get(i)] = Response.YELLOW;
				remainingSolutionLetters.remove(remainingSolutionLetters.indexOf(c));
			}
		}
		
		// Increment guess count and add this guess to the list of guesses
		this.puzzle.guesses[this.puzzle.guessCount++] = guess;

		return true;
	}
	
	/**
	 * @return true if one of the guesses matched the puzzle solution 
	 */
	public boolean isSolved() {
		return this.puzzle.solved;
	}
	
	/**
	 * @param guessNumber guess number to get a response from. guessNumber is 1-based, so passing a value of 
	 * 1 will retrieve the response from the first guess. Response values are null for guessNumber > guessCount
	 * @see {@link WordleEngine.Response}
	 * @return Array of Response objects.
	 */
	public Response[] getResponse(int guessNumber) {
		if(guessNumber > this.puzzle.guessCount) {
			return null;
		}
		return this.puzzle.responses[guessNumber-1];
	}
	
	/**
	 * @return number of guesses made against the current puzzle
	 */
	public int getGuessCount() {
		if(this.puzzle == null) {
			return 0;
		}
		return this.puzzle.guessCount;
	}
	
	/**
	 * Internally used by {@link WordleEngine} class to track guesses and responses to a Wordle game.
	 */
	private class Puzzle {
		private boolean solved;
		private int guessCount;
		private String solution;
		private boolean hardMode;
		private String[] guesses;
		private Response[][] responses;
		
		protected Puzzle(String solution, boolean hardMode) {
			this.solved = false;
			this.guessCount = 0;
			this.solution = solution.toUpperCase();
			this.hardMode = hardMode;
			this.guesses = new String[6];
			this.responses = new Response[6][5];
		}
	}
	
	/**
	 * Possible responses for each letter in a Wordle Guess
	 * <ul>
	 * <li>{@link #GRAY}</li>
	 * <li>{@link #YELLOW}</li>
	 * <li>{@link #GREEN}</li>
	 * </ul>
	 */
	public enum Response {
		/**
		 * The letter does not appear anywhere in the solution OR this is the nth appearance of the
		 * the letter in the guess and solution contains fewer than n instances of this letter.
		 */
		GRAY,
		/**
		 * The letter appears in the solution, but in a different location. 
		 * Further, if this is the nth appearance of the letter (counting left to right) 
		 * in the guess, then there are at least n instances of this letter in the solution
		 */
		YELLOW,
		/**
		 * This letter appears in the solution in the same position that it appears in the guess
		 */
		GREEN 
		
	}
	
}
