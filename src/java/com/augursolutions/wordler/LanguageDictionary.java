package com.augursolutions.wordler;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeMap;

/**
 * 
 * Dictionary implemented as an M-ary tree with Word objects attached to each WORD_ENDING node. All letters are stored in upper-case, so that
 * {@code Dicionary.add("foo")} and {@code Dicionary.add("FOO")} are equivalent. Only letters are allowed.
  <p>
 * Each node in the tree is one letter in one or more words, with the ASCII `start of text` value (02)
 * used for the root node and the `end of text` value (03) used for nodes that indicate the end of a word. 
 * Note that each node can have at most 27 children (A-Z and `end of text`). The children for each node are stored
 * in a {@link TreeMap} so that values are retrieved in alphabetical order.
 * <p>
 * As an example, consider a Dictionary object with the words "AT", "BE", "BED", and "BEST". The associated tree
 * would look like the image below, with (R) indicating the root node and (E) indicating nodes that mark the end of a word.
 * <pre>
 *     A-T-(E)
 *    /
 * (R) 
 *    \
 *     B   (E)
 *      \ / 
 *       E-D-(E)  
 *        \
 *         S-T-(E)
 * </pre>
 * Each word ending node is linked to a {@link Word} object that contains the spelling of the word as well as the definition 
 * and part(s) of speech (if provided when the dictionary is created)
 * <p>
 * For a more streamlined implementation that allows all characters (excluding the reserved ASCII values 02 and 03),
 * use the {@link HashDictionary} class
 * 
 * @author Steven Major
 *
 */
public class LanguageDictionary extends TreeDictionary {

	private static final long serialVersionUID = 1L;
	private Random rand;

	public LanguageDictionary() {
		this.rootNode = getRootNode();
	}
	
	/**
	 * Sets the random seed for the {@link #getRandomWord()} and {@link #getRandomWord(Word[])} methods
	 * @param seed Any {@code int} is valid
	 */
	public void setSeed(int seed) {
		this.rand = new Random(seed);
	}
	
	private Random getRand() {
		if(this.rand == null) {
			this.rand = new Random();
		}
		return this.rand;
	}
	
	@Override
	public HashDictionaryNode getRootNode() {
		if(this.rootNode == null) {
			this.rootNode = new LanguageDictionaryNode();
		}
		return this.rootNode;
	}
	
	/**
	 * Return the {@link Word} object (if the word exists in the
	 * dictionary) associated with the word. If the word is not
	 * in the dictionary, returns null.
	 * 
	 * Match is case-insensitive
	 * @param word Word to fetch from the Dictionary
	 * @return Word object with the matched word Returns null
	 * if no matching word was found in the dictionary.
	 */
	public Word getWord(String word) {
		if(word == null || word.isBlank()) {
			return null;
		}
		LanguageDictionaryNode lastLetter = (LanguageDictionaryNode)this.getNode(word);
		// If this word isn't in the dictionary, return null
		if(lastLetter == null) {
			return null;
		}
		// make sure we are at a word ending
		if(!lastLetter.endsWord())
			return null;
		return ((LanguageDictionaryNode)lastLetter.getWordEnding()).getWord();
	}

	/**
	 * Add a word to the {@linkDictionary}
	 * @param word Word to be added to the {@link Dictionary}
	 * @return {@code true} if the Word is added ({@code false} if the word already exists or if {@code word} is null or otherwise invalid)
	 */
	public boolean add(Word word) {
		if(word == null || word.getLetters() == null || word.getLetters().isEmpty())
			return false;
		HashDictionaryNode currentNode = this.getRootNode();
		for (int i = 0; i < word.length(); i++){
		    Character c = Character.valueOf(word.getLetters().charAt(i));
		    // If there is no entry for this letter, add it, otherwise grab the existing entry
		    if(!currentNode.getChildren().containsKey(c)) {
		    	currentNode = currentNode.addChild(c);
		    } else {
				currentNode = currentNode.getChildren().get(c);
			}
		}
	    // At the last letter, add an additional WORD_ENDING node with the Word object if one doesn't already exist
    	if(!currentNode.endsWord()) {
    		HashDictionaryNode wordEnding = currentNode.addWordEnding();
    		((LanguageDictionaryNode)wordEnding).setWord(word);
    		this.size++;
    	}
    	return true;
	}
	
	@Override
	public void remove(String word) {
		super.remove(word.toUpperCase());
	}
	
	@Override
	public HashDictionaryNode getNode(String word) {
		return super.getNode(word.toUpperCase());
	}
	
	/**
	 * Wrapper for {@link #addWord(Word)} for adding words
	 * with no definition
	 * @param word Word (letters only - no definition or parts of speech) to add to the dictionary
	 */
	@Override
	public boolean add(String word) {
		return add(new Word(word));
	}
	
	/**
	 * Get a random {@link Word} from the dictionary - all words are equally likely to be retrieved regardless of size
	 * @return A randomly chosen {@link Word}
	 */
	public Word getRandomWord() {
		Random r = this.getRand();
		int index = r.nextInt(this.getSize());
		Iterator<HashDictionaryNode> iter = this.iterator();
		HashDictionaryNode n = iter.next();
		for(int i=0; i<index; i++) {
			n = iter.next();
		}
		return ((LanguageDictionaryNode)n).getWord();
	}
	
	/**
	 * Returns a randomly chosen member of the provided {@code lookupTable} array. Ideally,
	 * this is generated by calling {@link #getRandomWordLookupTable()} first and then passing
	 * the array from that call in to this method with each call. 
	 * @param lookupTable Array of {@link Word} objects
	 * @return a randomly chosen member of the provided {@code lookupTable} array
	 */
	public Word getRandomWord(Word[] lookupTable) {
		Random r = this.getRand();
		int index = r.nextInt(lookupTable.length);
		return lookupTable[index];
	}
	
	/**
	 * Generate an array with every Word in the Dictionary. This is not efficient from a memory
	 * stand-point but allows for fast generation of random words from the dictionary
	 * @return An array with every {@link Word} in the dictionary (in alphabetical order)
	 */
	public Word[] getRandomWordLookupTable() {
	  Word[] wordTable = new Word[this.getSize()];
	  int i=0;
	  for(HashDictionaryNode n : this) {
		  wordTable[i++] = ((LanguageDictionaryNode)n).getWord();
	  }
	  return wordTable;
	}
	
	/**
	 * Node in a Dictionary tree. Nodes whose character is 
	 * <b>WORD_ENDING</b> should not contain any children and
	 * should be the only nodes that contain a definition.
	 * @author Steven Major
	 *
	 */
	public static class LanguageDictionaryNode extends TreeDictionaryNode {
		
		private static final long serialVersionUID = 1L;
		private Word word = null;

		protected LanguageDictionaryNode() {
			this(HashDictionaryNode.ROOT_NODE);
		}
		
		protected LanguageDictionaryNode(Character c) {
			this.setLetter(c);
			this.setChildren(this.initializeChildren());
		}
		
		public Word getWord( ) {
			return this.word;
		}
		
		public void setWord(Word word) { 
			this.word = word;
		}
		
		@Override
		protected HashDictionaryNode addChild(Character c) {
			LanguageDictionaryNode newChild = new LanguageDictionaryNode(c);
			newChild.setParent(this);
			newChild.setDistanceFromRoot(this.getDistanceFromRoot() + 1);
			this.getChildren().put(c, newChild);
			return newChild;
		}
		
    }
	
	public static void main(String[] args) {

		LanguageDictionary.testCases_Scrabble();
		LanguageDictionary.testCases_Simple();
		LanguageDictionary.testCases_Iterator();
		LanguageDictionary.testCases_Random();
			
		/*
		LanguageDictionary simple = new LanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(simple,Path.of("./test/dictionaries","small_no_definitions.txt"), false);
		for(TreeDictionaryNode n : simple) {
			System.out.println(((LanguageDictionaryNode)n).getWord());
		}
		*/
	}

	/*
	 * Tests on the Scrabble 2023 Dictionary
	 */
	private static void testCases_Scrabble() {
		LanguageDictionary dictionary = new LanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","NWL2023.txt"));
		
		// TEST 1 - Verify size
		assert (dictionary.getSize() == 196601) : "Failed Scrabble 2023 size test";
		
		// TEST 2 - Verify that "NOMENCLATOR" is in this dictionary
		assert (dictionary.getWord("NOMENCLATOR") != null) :  "Failed find word (NOMENCLATOR) test";
		
		// TEST 3 - Verify that "WRDDNE" is not in this dictionary
		assert (dictionary.getWord("WRDDNE") == null) : "Failed non-existenent word (WRDDNE) test";

	}
	
	/*
	 * Tests on small test dictionary
	 */
	private static void testCases_Simple() {

		System.out.println("testCases_Simple: running ...");
		LanguageDictionary dictionary = new LanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","small_no_definitions.txt"));
		
		// TEST 1 - GUARD appears twice so there are 15 entries but actual size should be 14
		assert (dictionary.getSize() == 14) : "Failed double word in dictionary file test";
		System.out.println(dictionary.getSize());
		
		// TEST 2 - Verify size after adding a word
		assert (dictionary.getWord("BATS") == null) : "BATS is already in the simple dictionary example";
		dictionary.add("BATS");
		assert (dictionary.getSize() == 15) : "Failed add word test";
		dictionary.printAll();
		System.out.println(dictionary.getSize());
		System.out.println("-------");
		
		// TEST 3 - Verify size after removing words
		dictionary.remove("bat");
		dictionary.remove("bats");
		dictionary.printAll();
		System.out.println(dictionary.getSize());
		assert (dictionary.getSize() == 13) : "Failed dictionary size test after word removal";
		System.out.println("testCases_Simple: PASSED");
		
	}
	
	private static void testCases_Iterator() {
		System.out.println("testCases_Iterator: running ...");
		// TEST 1 - Add some 'words' to a dictionary and verify that the iterator gets them all and in the correct order
		LanguageDictionary testDictionary = new LanguageDictionary();
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
		Iterator<HashDictionaryNode> wordIter = testDictionary.iterator();
		int i = 0;
		while(wordIter.hasNext()) {
			String testWord = testWords[testWordOrder[i++]];
			String iterWord = ((LanguageDictionaryNode)wordIter.next()).getWord().getLetters();
			System.out.println(iterWord);
			assert(testWord.equals(iterWord)) : "Next word should be '" + testWord + "' but found '" + iterWord + "'";
		}
		
		//TEST 2 - next() should throw a NoSuchElementException if called when there are no more words. Try on an empty dictionary
		LanguageDictionary emptyDictionary = new LanguageDictionary();
		wordIter = emptyDictionary.iterator();
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
		assert(catchFailure.isEmpty()) : "Tried calling next() on an empty dictionary, expected a NoSuchElementException, instead got '" + catchFailure + "'";
		
		//TEST 3 - next() should throw a NoSuchElementException if called when there are no more words. Try on a dictionary with some words in it
		wordIter = testDictionary.iterator();
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
		assert(catchFailure.isEmpty()) : "Tried calling next() more times than there are words in the dictionary, expected a NoSuchElementException, instead got '" + catchFailure + "'";
		
		System.out.println("testCases_Iterator: PASSED");
	}
	
	private static void testCases_Random() {
		System.out.println("testCases_Random: running ...");
		// TEST 1 - Create a Dictionary with some words and randomly draw from it repeatedly. Verify that each word is hit
		// (roughly) the same number of times (number_of_draws/number_of_words +/- some error)
		LanguageDictionary d = new LanguageDictionary();
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
			assert( count > p_lower && count < p_upper ) : "getRandomWord(lookupTable) Test: Word '" +  w + "' appeared " + count + " times, limits were: (" + p_lower + " , " + p_upper + ")";
		}
		System.out.println("testCases_Random: PASSED");
	}
}