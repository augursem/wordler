package com.augursolutions.wordlerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.augursolutions.wordler.DictionaryLoadUtils;
import com.augursolutions.wordler.TreeMapLanguageDictionary;
import com.augursolutions.wordler.Word;


public class WordlerDictionaryTest {
	
	/**
	 * Basic test of WordleDictionary and WordleSoltuions:
	 *  - Verify that all Wordle solutions are in WordleDictionary 
	 *  - Verify that Wordle dictionary is a subset of SCRABBLE dictionary
	 */
	@Test
	public void wordleDictionaryTest() throws Exception {
		System.out.println("WordleTest / wordleDictionaryTest ...");
		TreeMapLanguageDictionary scrabbleDictionary = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(scrabbleDictionary,Path.of("./test/dictionaries","NWL2023.txt"));
		TreeMapLanguageDictionary wordleDictionary = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		TreeMapLanguageDictionary wordleSolutions = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleSolutions,Path.of("./dictionaries","WordleSolutions.txt"));
		
		// TEST 1 - all Wordle Solutions are in Wordle dictionary
		for(Word w : wordleSolutions) {
			assertTrue(wordleDictionary.contains(w.getLetters()), () -> "Failed Wordler Solutions test: solution word '" + w + "' is not in Wordler dictionary");
		}
	}
}
