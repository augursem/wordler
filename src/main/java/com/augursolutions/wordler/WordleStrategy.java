package com.augursolutions.wordler;

/**
 * Simple interface describing methods that a Wordle Solver needs to implement
 * @author Steven Major
 * @see {@link SimpleWordleStrategy}
 */
public interface WordleStrategy {
	
	/**
	 * Initial guess
	 * @return Some 5 letter word
	 */
	public String firstGuess();
	
	/**
	 * Guess made after the first guess. Only takes the previous guess and response rather
	 * than the whole history - if the list of possible solutions is updated with each guess,
	 * this is all you need. If your strategy needs the full history of guesses and responses
	 * for some reason, then it will have to keep track of them internally
	 * @param previousGuess Last word guessed
	 * @param previousResponse response from last word guessed
	 * @return The next five letter word to guess
	 */
	public String nextGuess(String previousGuess, WordleEngine.Response[] previousResponse);
}
