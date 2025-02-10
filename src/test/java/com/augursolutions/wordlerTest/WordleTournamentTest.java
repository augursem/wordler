package com.augursolutions.wordlerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.augursolutions.wordler.DictionaryLoadUtils;
import com.augursolutions.wordler.SimpleWordleStrategy;
import com.augursolutions.wordler.TreeSetDictionary;
import com.augursolutions.wordler.WordleStrategy;
import com.augursolutions.wordler.WordleTournament;

public class WordleTournamentTest {
	private static final Logger LOGGER = Logger.getLogger( WordleTournamentTest.class.getName() );
	

	@Test
	public void simpleTournamentTest() throws Exception {
		LOGGER.info("WordleTournamentTest / simpleTournamentTest ...");
		//TreeSetDictionary wordleDictionary = new TreeSetDictionary();
		//DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		TreeSetDictionary wordleSolutions = new TreeSetDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleSolutions,Path.of("./dictionaries","WordleSolutions.txt"));
		List<WordleStrategy> competitors = new ArrayList<>();
		competitors.add(new SimpleWordleStrategy(wordleSolutions));
		
		WordleTournament tourney = new WordleTournament(wordleSolutions, wordleSolutions, competitors);
		// Make sure it runs cleanly
		try {
			tourney.run();
		} catch(Exception e) {
			assertTrue(false, () -> "Exception thrown while running the tournament"); 
		}
		
		// Sanity check - scoreCard values add to number of puzzles tested
		int[] scoreCard = tourney.getScoreCards().get(0);
		int count = 0;
		for(int i : scoreCard) { count += i; }
		assertTrue(wordleSolutions.getSize() == count, () -> "Scorecard count does not match number of puzzles in tournament"); 
		
	}

}
