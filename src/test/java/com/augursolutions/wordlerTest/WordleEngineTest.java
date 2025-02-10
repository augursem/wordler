package com.augursolutions.wordlerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.augursolutions.wordler.DictionaryLoadUtils;
import com.augursolutions.wordler.TreeMapLanguageDictionary;
import com.augursolutions.wordler.WordleEngine;

public class WordleEngineTest {

	private static final Logger LOGGER = Logger.getLogger( WordleEngineTest.class.getName() );
	
	/**
	 * Basic test of WordleDictionary and WordleSoltuions:
	 *  - Verify that all Wordle solutions are in WordleDictionary 
	 *  - Verify that Wordle dictionary is a subset of SCRABBLE dictionary
	 */
	@Test
	public void wordleEngineBasicTest() throws Exception {
		LOGGER.info("WordleEngineTest / wordleEngineBasicTest ...");
		TreeMapLanguageDictionary wordleDictionary = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		WordleEngine wordleEngine = new WordleEngine(wordleDictionary);
		
		// TEST 1 - Try to create puzzles that would fail
		String[] badGuesses = new String[] {
				"TINY",
				"DNEDD",
				"TOOBIG",
				"!@#345"
		};
		for(String w: badGuesses) {
			assertTrue(!wordleEngine.newPuzzle(w), () -> "Should have failed to use the word '" + w + "' as a Wordle solution");
		}
		
		// Check responses to guesses for valid puzzles
		WordleEngine.Response[] expectedResponses = new WordleEngine.Response[5];
		wordleEngine.newPuzzle("STAVE");
		
		assertTrue(wordleEngine.guessWord("TOTAL"), () -> "TOTAL is a valid guess for the puzzle with solution STAVE");
		assertTrue(!wordleEngine.isSolved(), () -> "Solution is STAVE, guess was TOTAL; puzzle is not solved but isSolved() returned TRUE");
		expectedResponses[0] = WordleEngine.Response.YELLOW;
		expectedResponses[1] = WordleEngine.Response.GRAY;
		expectedResponses[2] = WordleEngine.Response.GRAY;
		expectedResponses[3] = WordleEngine.Response.YELLOW;
		expectedResponses[4] = WordleEngine.Response.GRAY;
		for(int i=0; i<5; i++) {
			assertTrue(wordleEngine.getResponse(1)[i] == expectedResponses[i], () -> "For guess TOTAL, response did not match expected response");
		}
		
		assertTrue(wordleEngine.guessWord("FIRST"), () -> "FIRST is a valid guess for the puzzle with solution STAVE");
		assertTrue(!wordleEngine.isSolved(), () -> "Solution is STAVE, guess was FIRST; puzzle is not solved but isSolved() returned TRUE");
		expectedResponses[0] = WordleEngine.Response.GRAY;
		expectedResponses[1] = WordleEngine.Response.GRAY;
		expectedResponses[2] = WordleEngine.Response.GRAY;
		expectedResponses[3] = WordleEngine.Response.YELLOW;
		expectedResponses[4] = WordleEngine.Response.YELLOW;
		for(int i=0; i<5; i++) {
			assertTrue(wordleEngine.getResponse(2)[i] == expectedResponses[i], () -> "For guess FIRST, response did not match expected response");
		}
		
		assertTrue(wordleEngine.guessWord("TONAL"), () -> "TONAL is a valid guess for the puzzle with solution STAVE");
		assertTrue(!wordleEngine.isSolved(), () -> "Solution is STAVE, guess was TOPIC; puzzle is not solved but isSolved() returned TRUE");
		expectedResponses[0] = WordleEngine.Response.YELLOW;
		expectedResponses[1] = WordleEngine.Response.GRAY;
		expectedResponses[2] = WordleEngine.Response.GRAY;
		expectedResponses[3] = WordleEngine.Response.YELLOW;
		expectedResponses[4] = WordleEngine.Response.GRAY;
		for(int i=0; i<5; i++) {
			assertTrue(wordleEngine.getResponse(3)[i] == expectedResponses[i], () -> "For guess TONAL, response did not match expected response");
		}
		
		assertTrue(wordleEngine.guessWord("STAVE"), () -> "STAVE is a valid guess for the puzzle with solution STAVE");
		assertTrue(wordleEngine.isSolved(), () -> "Solution is STAVE, guess was STAVE; isSolved() returned FALSE");
		expectedResponses[0] = WordleEngine.Response.GREEN;
		expectedResponses[1] = WordleEngine.Response.GREEN;
		expectedResponses[2] = WordleEngine.Response.GREEN;
		expectedResponses[3] = WordleEngine.Response.GREEN;
		expectedResponses[4] = WordleEngine.Response.GREEN;
		for(int i=0; i<5; i++) {
			assertTrue(wordleEngine.getResponse(4)[i] == expectedResponses[i], () -> "For guess STAVE, response did not match expected response");
		}
		
		wordleEngine.guessWord("STAVE");
		wordleEngine.guessWord("STAVE");
		assertTrue(wordleEngine.getGuessCount() == 6, () -> "After six guesses, number of guesses was not six");
		
	}

}
