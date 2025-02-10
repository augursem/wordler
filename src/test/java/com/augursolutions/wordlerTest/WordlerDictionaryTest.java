package com.augursolutions.wordlerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.augursolutions.wordler.DictionaryLoadUtils;
import com.augursolutions.wordler.TreeMapLanguageDictionary;


public class WordlerDictionaryTest {

	private static final Logger LOGGER = Logger.getLogger( WordlerDictionaryTest.class.getName() );
	
	/**
	 * Basic test of WordleDictionary and WordleSoltuions:
	 *  - Verify that all Wordle solutions are in WordleDictionary 
	 *  - Verify that Wordle dictionary is a subset of SCRABBLE dictionary
	 */
	@Test
	public void wordleDictionaryTest() throws Exception {
		LOGGER.info("WordleTest / wordleDictionaryTest ...");
		TreeMapLanguageDictionary scrabbleDictionary = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(scrabbleDictionary,Path.of("./test/dictionaries","NWL2023.txt"));
		TreeMapLanguageDictionary wordleDictionary = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		TreeMapLanguageDictionary wordleSolutions = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleSolutions,Path.of("./dictionaries","WordleSolutions.txt"));
		
		// TEST 1 - all Wordle Solutions are in Wordle dictionary
		for(String w: wordleSolutions) {
			assertTrue(wordleDictionary.contains(w), () -> "Failed Wordler Solutions test: solution word '" + w + "' is not in Wordler dictionary");
		}
	}
}
