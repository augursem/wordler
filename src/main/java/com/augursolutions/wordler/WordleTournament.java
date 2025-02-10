package com.augursolutions.wordler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class for comparing different Wordle strategies.
 * @author Steven Major
 *
 */
public class WordleTournament {
	
	private static final Logger LOGGER = Logger.getLogger( WordleTournament.class.getName() );

	private Dictionary fullDictionary;
	private Dictionary solutions;
	private List<? extends WordleStrategy> strategies;
	private List<int[]> scoreCards;
	
	/**
	 * @param fullDictionary List of all valid guess words
	 * @param solutions List of all possible solutions (should be a subset of fullDictionary)
	 * @param strategies List of {@link WordleStrategy} objects to compare
	 */
	public WordleTournament(Dictionary fullDictionary, Dictionary solutions, List<? extends WordleStrategy> strategies) {
		this.fullDictionary = fullDictionary;
		this.solutions = solutions;
		this.strategies = strategies;
		this.scoreCards = new ArrayList<>();
	}
	
	/**
	 * For each strategy, creates a puzzle using each of the words in {@code solutions} and tracks how
	 * many guesses the strategy takes for that solution.
	 */
	public void run() {
		WordleEngine engine = new WordleEngine(this.fullDictionary);
		for(WordleStrategy strategy : this.strategies) {
			LOGGER.info("Stepping into the ring: '" + strategy.getClass().getSimpleName() + "'");
			
			//Initialize scorecard for this strategy
			int[] scorecard = new int[7];
			for(int i = 0; i< 7; i++) {scorecard[i] = 0;}
			
			// For each solution, try to solve using this strategy
			for(String solution : this.solutions) {
				engine.newPuzzle(solution);
				engine.guessWord(strategy.firstGuess());
				while(!engine.isSolved() && engine.getGuessCount() < 6) {
					engine.guessWord(strategy.nextGuess(engine.getGuess(engine.getGuessCount()),engine.getResponse(engine.getGuessCount())));
				}
				// How many guesses for this puzzle (7 guesses is failure)
				int numGuess = engine.getGuessCount();
				if(!engine.isSolved()) {
					numGuess += 1;
				}
				scorecard[numGuess-1] = scorecard[numGuess-1] + 1;
			}
			this.scoreCards.add(scorecard);
		}
	}
	
	/**
	 * For each strategy, print out the distribution of solve times
	 */
	public void printResults() {
		int sIndex = 0;
		for(WordleStrategy strategy : this.strategies) {
			System.out.println("STRATEGY: '" + strategy.getClass().getSimpleName() + "'");
			
			double totalPuzzles = 0.0;
			int[] scoreCard = this.scoreCards.get(sIndex);
			for(int i=0; i<7;i++) {
				totalPuzzles+=scoreCard[i];
			}
			
			for(int i=0; i<7;i++) {
				// percentage to 2 decimal places
				double p = Math.round(scoreCard[i] / totalPuzzles * 10000.0)/100.0;
				String guess_es = " guesses: ";
				if(i==0)
					guess_es = " guess:   ";
				if(i < 6) {
					System.out.println("\t" + (i+1) + guess_es + scoreCard[i] + " (" + p + "%)");
				} else {
					System.out.println("\tfailed:    " + scoreCard[i] + " (" + p + "%)");
				}
			}
			sIndex++;
		}
	}
	
	public List<int[]> getScoreCards() {
		return this.scoreCards;
	}
	
	public static void main(String[] args) {

		TreeSetDictionary wordleDictionary = new TreeSetDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		TreeSetDictionary wordleSolutions = new TreeSetDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleSolutions,Path.of("./dictionaries","WordleSolutions.txt"));
		List<WordleStrategy> competitors = new ArrayList<>();
		competitors.add(new SimpleWordleStrategy(wordleDictionary));
		
		WordleTournament tourney = new WordleTournament(wordleDictionary, wordleSolutions, competitors);
		tourney.run();
		tourney.printResults();
	}
}
