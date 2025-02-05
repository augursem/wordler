package com.augursolutions.wordlerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import com.augursolutions.wordler.Dictionary;
import com.augursolutions.wordler.DictionaryLoadUtils;
import com.augursolutions.wordler.NYTWordlerUtils;
import com.augursolutions.wordler.TreeMapLanguageDictionary;
import com.augursolutions.wordler.Word;
import com.augursolutions.wordler.HashMapDictionary.HashDictionaryNode;
import com.augursolutions.wordler.TreeMapDictionary;
import com.augursolutions.wordler.TreeMapLanguageDictionary.LanguageDictionaryNode;

public class DictionaryTest { 

	private static Set<Class<? extends Dictionary>> dictionaryClasses = null;
	private static Set<Class<? extends TreeMapDictionary>> treeMapDictionaryClasses = null;
	
	private static final Logger LOGGER = Logger.getLogger( NYTWordlerUtils.class.getName() );
	
	static {
		// Collect all extensions of the Dictionary class
		Reflections reflections = new Reflections("com.augursolutions");    
		dictionaryClasses = reflections.getSubTypesOf(Dictionary.class);  
		treeMapDictionaryClasses = reflections.getSubTypesOf(TreeMapDictionary.class);
	}
	
	/*
	 * Tests on the Scrabble 2023 Dictionary
	 */
    @Test
	public void scrabbleTest() throws Exception {
    	LOGGER.info("DictionaryTest / scrabbleTest ...");
		int dictionarySize =196601;
		for(Class<? extends Dictionary> klass : dictionaryClasses) {
			LOGGER.info("\t" + klass.getSimpleName() + " ...");
			Dictionary dictionary = klass.getConstructor().newInstance();
			DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","NWL2023.txt"));

			// TEST 1 - Verify size
			assertTrue(dictionary.getSize() == dictionarySize, () -> "Failed Scrabble 2023 size test: expected Dictionary Size " + dictionarySize + ", but size was: " + dictionary.getSize());
			
			// TEST 2 - Verify that "NOMENCLATOR" is in this dictionary
			assertTrue(dictionary.getWord("NOMENCLATOR") != null, ()-> "Failed to find word 'NOMENCLATOR' after adding it");
			
			// TEST 3 - Verify that "WRDDNE" is not in this dictionary
			assertTrue(dictionary.getWord("WRDDNE") == null, ()-> "Failed non-existenent word (WRDDNE) test");
		}
	}
	
	/*
	 * Tests on small test dictionary
	 */
    @Test
    public void simpleTest() throws Exception {
    	LOGGER.info("DictionaryTest / simpleTest ...");
		for(Class<? extends Dictionary> klass : dictionaryClasses) {
			LOGGER.info("\t" + klass.getSimpleName() + " ...");
			Dictionary dictionary = klass.getConstructor().newInstance();
			DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","small_no_definitions.txt"));
			
			// TEST 1 - GUARD appears twice so there are 15 entries but actual size should be 14
			assertTrue(dictionary.getSize() == 14, ()-> "Failed double word in dictionary file test");
			
			// TEST 2 - Verify size after adding a word
			assertTrue(dictionary.getWord("BATS") == null, ()-> "BATS is already in the simple dictionary example");
			dictionary.add("BATS");
			assertTrue(dictionary.getSize() == 15, ()-> "Failed add word test");
			
			// TEST 3 - Verify size after removing words
			dictionary.remove("BAT");
			dictionary.remove("BATS");
			assertTrue(dictionary.getSize() == 13, ()-> "Failed dictionary size test after word removal");
		}
	}

	/*
	 * Tests on Dictionary class iterators
	 */
    @Test
    public void alphabeticalIteratorTest() throws Exception {
    	LOGGER.info("DictionaryTest / alphabeticalIteratorTest ...");
		for(Class<? extends TreeMapDictionary> klass : treeMapDictionaryClasses) {
			LOGGER.info("\t" + klass.getSimpleName() + " ...");
			TreeMapDictionary testDictionary = klass.getConstructor().newInstance();
			
			// TEST 1 - Add some 'words' to a dictionary and verify that the iterator gets them all and in the correct order
			String[] testWords = {
				"ZA",
				"A",
				"AAA",
				"AA",
				"BA",
				"AB"
			};
			int[] testWordOrder = {
				1,
				3,
				2,
				5,
				4,
				0
			};
			for(String word : testWords) {
				testDictionary.add(word);
			}
			Iterator<HashDictionaryNode> wordIter = testDictionary.nodeIterator();
			int i = 0;
			while(wordIter.hasNext()) {
				String testWord = testWords[testWordOrder[i++]];
				String iterWord = ((LanguageDictionaryNode)wordIter.next()).getWord().getLetters();
				assertTrue(testWord.equals(iterWord), ()-> "Next word should be '" + testWord + "' but found '" + iterWord + "'");
			}
			
			//TEST 2 - next() should throw a NoSuchElementException if called when there are no more words. Try on an empty dictionary
			TreeMapLanguageDictionary emptyDictionary = new TreeMapLanguageDictionary();
			wordIter = emptyDictionary.nodeIterator();
			String catchFailure = "Unknown Problem";
			try {
				wordIter.next();
				catchFailure = "No Exception Thrown";
			}
			catch(NoSuchElementException nse) {
				catchFailure = "";
			}
			catch(Exception e) {
				catchFailure = "Threw Other Exception";
			}
			String catchFailure1 = catchFailure;
			assertTrue(catchFailure1.isEmpty(), ()-> "Tried calling next() on an empty dictionary, expected a NoSuchElementException, instead got '" + catchFailure1 + "'");
			
			//TEST 3 - next() should throw a NoSuchElementException if called when there are no more words. Try on a dictionary with some words in it
			wordIter = testDictionary.nodeIterator();
			catchFailure = "Unknown Problem";
			try {
				for(int idx=0; idx<testDictionary.getSize(); idx++) {
					wordIter.next();
				}
				wordIter.next();
				catchFailure = "No Exception Thrown";
			}
			catch(NoSuchElementException nse) {
				catchFailure = "";
			}
			catch(Exception e) {
				catchFailure = "Threw Other Exception";
			}
			String catchFailure2 = catchFailure;
			assertTrue(catchFailure2.isEmpty(), ()-> "Tried calling next() more times than there are words in the dictionary, expected a NoSuchElementException, instead got '" + catchFailure2 + "'");
		}
	}
	
    /**
     * Test random word generation by looking at word distribution from a large sample
     */
    @Test
    public void randomizationTest() throws Exception {
    	LOGGER.info("DictionaryTest / randomizationTest ...");
		for(Class<? extends Dictionary> klass : dictionaryClasses) {
			LOGGER.info("\t" + klass.getSimpleName() + " ...");
			
			// TEST 1 - Create a Dictionary with some words and randomly draw from it repeatedly. Verify that each word is hit
			// (roughly) the same number of times (number_of_draws/number_of_words +/- some error)
			TreeMapLanguageDictionary d = new TreeMapLanguageDictionary();
			d.setSeed(12345);
			int nDraws = 5000;
			double errorAllowance = 0.05;
			String[] wordStrings = {
				"one",
				"dos",
				"amigos",
				"zebreafish"
			};
			Word[] words = new Word[wordStrings.length];
			Map<Word,Integer> wordCounts = new HashMap<>();
			for(int i=0; i<wordStrings.length; i++) {
				d.add(wordStrings[i]);
				words[i] = d.getWord(wordStrings[i]);
				wordCounts.put(words[i], 0);
			}
			
			for(int i=0; i<nDraws; i++) {
				Word w = d.getRandomWord();
				wordCounts.put(w, wordCounts.get(w)+1);
			}
			
			double p_lower = Math.floor( (1.0-errorAllowance)*nDraws/wordStrings.length );
			double p_upper = Math.ceil( (1.0+errorAllowance)*nDraws/wordStrings.length );
			for(Word w : words) {
				int count = wordCounts.get(w);
				assert( count > p_lower && count < p_upper ) : "getRandomWord() Test: Word '" +  w + "' appeared " + count + " times, limits were: (" + p_lower + " , " + p_upper + ")";
			}
			
			// TEST 2 - Repeat TEST 1 but with a lookup table
			Word[] allWords = d.getRandomWordLookupTable();
			for(int i=0; i<wordStrings.length; i++) {
				wordCounts.put(words[i], 0);
			}
			
			for(int i=0; i<nDraws; i++) {
				Word w = d.getRandomWord(allWords);
				wordCounts.put(w, wordCounts.get(w)+1);
			}
			
			for(Word w : words) {
				int count = wordCounts.get(w);
				assertTrue( count > p_lower && count < p_upper , ()-> "getRandomWord(lookupTable) Test: Word '" +  w + "' appeared " + count + " times, limits were: (" + p_lower + " , " + p_upper + ")");
			}
		}
	}
}
