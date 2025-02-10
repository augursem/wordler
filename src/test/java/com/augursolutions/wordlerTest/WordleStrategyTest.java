package com.augursolutions.wordlerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.augursolutions.wordler.DictionaryLoadUtils;
import com.augursolutions.wordler.SimpleWordleStrategy;
import com.augursolutions.wordler.TreeSetDictionary;
import com.augursolutions.wordler.WordleEngine;

public class WordleStrategyTest {

	private static final Logger LOGGER = Logger.getLogger( WordleStrategyTest.class.getName() );

	@Test
	public void simpleStrategyTest() throws Exception {
		LOGGER.info("WordleStrategyTest / simpleStrategyTest ...");

		TreeSetDictionary wordleDictionary = new TreeSetDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		WordleEngine engine = new WordleEngine(wordleDictionary);
		SimpleWordleStrategy strategy = new SimpleWordleStrategy(wordleDictionary);
		engine.newPuzzle("LEVEL");
		String guess = strategy.firstGuess("GEESE");
		int previousSize = strategy.getReducedDictionary().getSize();
		int size = previousSize;
		while(!engine.isSolved() && engine.getGuessCount() < 6) {
			try {
				engine.guessWord(guess);
				guess = strategy.nextGuess(guess, engine.getResponse(engine.getGuessCount()));
				size = strategy.getReducedDictionary().getSize();
				assertTrue(size < previousSize, () -> "SimpleWordleStrategy did not reduce the dictionary size after making a guess.");
				assertTrue(size != 0, () -> "SimpleWordleStrategy produced an empty reduced dictionary");
				previousSize = size;
			} catch(Exception e) {
				assertTrue(false, () -> "Exception thrown while making a guess"); 
			}
		}
	}
}
	
