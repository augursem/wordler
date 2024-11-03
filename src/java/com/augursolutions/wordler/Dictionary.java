package com.augursolutions.wordler;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * Dictionary implemented as an M-ary tree. All letters are stored in upper-case, so that
 * {@code Dicionary.add("foo")} and {@code Dicionary.add("FOO")} are equivalent. Only letters are allowed.
 * <p>
 * Each node in the tree is one letter in one or more words, with the ASCII `start of text` value (02)
 * used for the root node and the `end of text` value (03) used for nodes that indicate the end of a word. 
 * Note that each node can have at most 27 children (A-Z and `end of text`). The children for each node are stored
 * in a {@link TreeMap} so that values are retrieved in alphabetical order.
 * <p>
 * As an example, consider a Dictionary object with the words "AT", "BE", "BED", and "BEST". The associated tree
 * would look like the image below, with (R) indicating the root node and (E) indicating nodes that mark the end of a word.
 * <p>
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
 * use the {@link SimpleDictionary} class
 * 
 * @author Steven Major
 *
 */
public class Dictionary {
	private DictionaryNode rootNode;
	private int size = 0;

	public Dictionary() {
		rootNode = new DictionaryNode(DictionaryNode.ROOT_NODE);
	}
	
	public Dictionary(String word) {
		this();
		this.addWord(word);
	}

	public Dictionary(String[] words) {
		this();
		if(words == null)
			return;
		for(String word : words) {
			this.addWord(word);
		}
	}
	
	public Dictionary(Set<String> words) {
		this();
		if(words == null)
			return;
		for(String word : words) {
			this.addWord(word);
		}
	}
	
	/**
	 * Gets the number of words in the dictionary (specifically the number of nodes
	 * whose letter is DictionaryNode.WORD_ENDING
	 * @return number of words in the dictionary
	 */
	public int getSize() {
		return this.size;
	}
	
	/**
	 * Gets the root node - if none exists, creates a new empty node as the root.
	 * @return The root node of the Dictionary
	 */
	public DictionaryNode getRootNode() {
		if(this.rootNode == null) {
			this.rootNode = new DictionaryNode(DictionaryNode.ROOT_NODE);
		}
		return this.rootNode;
	}
	
	/**
	 * Get the {@link DictionaryNode} associated with a word fragment. For example, if the word "DOG" is in the
	 * {@link Dictionary}, then {@code getNode("DO")} will return the node whose letter is "O" with parent "D" and child "G"
	 * (other children may be present)
	 * @param wordFragment partial word (can be full word) to fetch the node for. 
	 * @return Corresponding node of the last letter in {@code wordFragment}. NULL if the word fragment is not in the dictionary.
	 */
	public DictionaryNode getNode(String wordFragment) {
		if(wordFragment == null || wordFragment.isEmpty()) {
			return null;
		}
		DictionaryNode node = this.getRootNode();
		// Go through letters fetching associated node for each
		for(int i=0; i<wordFragment.length(); i++) {
			Character c = wordFragment.charAt(i);
			// If there is no corresponding node, then the word fragment is not in the dictionary - return  null
			if(!node.getChildren().containsKey(c)) {
				return null;
			}
			node = node.getChildren().get(c);
		}
		return node;
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
		// All words are stored in upper case
		word = word.toUpperCase();
		DictionaryNode currentNode = this.rootNode;
		for(int i=0; i<word.length(); i++) {
			Character c = word.charAt(i);
			if(!currentNode.getChildren().containsKey(c)) {
				//Word is not in the dictionary
				return null;
			}
			currentNode = currentNode.getChildren().get(c);
			// Check for bad node
			if(currentNode == null || !currentNode.hasChildren()) {
				System.out.println("ERROR: reached null or empty node while looking up '" + word + "' at character " + (i+1));
				return null;
			}
		}
		// After going through all of the letters, make sure we are at a word ending
		if(!currentNode.endsWord()) {
			return null;
		}
		return currentNode.getChildren().get(DictionaryNode.WORD_ENDING).getWord();
	}

	/**
	 * Add a word to the {@link Dictionary}; the {@link Dictionary} object is 
	 * modified in the process.
	 * @param word Word to be added to the {@link Dictionary}
	 */
	public boolean addWord(Word word) {
		if(word == null || word.getLetters() == null || word.getLetters().isEmpty())
			return false;
		DictionaryNode currentNode = this.rootNode;
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
    		DictionaryNode wordEnding = currentNode.addChild(DictionaryNode.WORD_ENDING);
    		wordEnding.setWord(word);
    		this.size++;
    	}
    	return true;
	}
	
	/**
	 * Wrapper for {@link #addWord(Word)} for adding words
	 * with no definition
	 * @param word Word (letters only - no definition or parts of speech) to add to the dictionary
	 */
	public void addWord(String word) {
		this.addWord(new Word(word));
	}

	/**
	 * Remove a word from the {@link Dictionary}; the {@link Dictionary} object is 
	 * modified in the process. If {@code word} is not present in the dictionary, it is
	 * left unmodified
	 * @param word Word to be removed from the {@link Dictionary}
	 */
	public void removeWord(String word) {

		if(word == null || word.isEmpty())
			return;
		word = word.toUpperCase();
		
		// First collect a list of nodes corresponding to each letter in the word
		LinkedList<DictionaryNode> nodes = new LinkedList<>();
		DictionaryNode currentNode = this.rootNode;
		for (int i = 0; i < word.length(); i++){
		    Character c = Character.valueOf(word.charAt(i));
		    // Return if the word is not in the dictionary
		    if(!currentNode.getChildren().containsKey(c)) {
		    	return;
		    }
		    currentNode = currentNode.getChildren().get(c);
    		nodes.push(currentNode);
		}
		
		//If we are at the end of the list and this is not the end of some word, return
		if(!currentNode.endsWord()) {
	    	return;
		}
		
		// Now, go through the list of nodes from end to start. At each one, remove
		// the corresponding character from its children (starting with removing WORD_ENDING from
		// the node corresponding to the last letter). If after doing this that node has no children,
		// i.e. no other words are using this node, move on to the next node and remove the now
		// childless node from it's children. Repeat until all letters are removed or until we reach a 
		// node that still has children after removing a letter.
		Character currentChar = DictionaryNode.WORD_ENDING;
		while(!nodes.isEmpty()) {
			currentNode = nodes.pop();
			currentNode.getChildren().remove(currentChar);
			// If there are other child nodes, then this node is part of another word - leave it (stop)
			if(currentNode.hasChildren()) {
				break;
			}
			currentChar = currentNode.getLetter();
		}
		this.size--;
	}
	
	/**
	 * Prints every word in the {@link Dictionary} to the console
	 * in alphabetical order. Uses recursion.
	 */
	public void printAll() {
		printAll(this.rootNode);
	}
	
	/**
	 * Prints every word in the {@link Dictionary} that has {@code node} as its parent.
	 * @param node Current node in the recursion
	 */
	public void printAll(DictionaryNode node) {
		// Catch bad inputs / malformed dictionary node
		if(node == null || node.getLetter() == null || !node.hasChildren()) {
			return;
		}
		// For each of this node's children: if the child is a WORD_ENDING character, then
		// print the current word. Otherwise, call printAll on the child
		for(Map.Entry<Character, DictionaryNode> entry : node.getChildren().entrySet()) {
			DictionaryNode childNode = entry.getValue();
			if(childNode.getLetter() == DictionaryNode.WORD_ENDING)
				System.out.println(childNode.getWord().getLetters());
			else
				printAll(childNode);
		}
	}
	
	/**
	 * Node in a Dictionary tree. Nodes whose character is 
	 * <b>WORD_ENDING</b> should not contain any children and
	 * should be the only nodes that contain a definition.
	 * @author Steven Major
	 *
	 */
	public class DictionaryNode  {
		protected static final Character WORD_ENDING = (char)03;
		protected static final Character ROOT_NODE = (char)02;
		
		private Character letter = null;
		private Map<Character, DictionaryNode> children;
		private DictionaryNode parent = null;
		protected int distanceFromRoot = 0;
		private Word word = null;
		
		
		public DictionaryNode(Character c, DictionaryNode n) {
			this.letter = c;
			this.parent = n;
			if(n != null) {
				this.distanceFromRoot = n.distanceFromRoot + 1;
			}
			this.children = new TreeMap<>();
		}
		
		public DictionaryNode(Character c) {
			this(c, null);
		}
		
		public Character getLetter() {
			return this.letter;
		}
		
		public void setLetter(Character c) {
			this.letter = c;
		}
		 
		public Map<Character, DictionaryNode> getChildren() {
			return this.children;
		}
		
		public DictionaryNode getParent() {
			return this.parent;
		}
		
		public Word getWord( ) {
			return this.word;
		}
		
		public void setWord(Word word) { 
			this.word = word;
		}
		
		public boolean hasChildren() { 
			return (this.children != null && !this.children.isEmpty()); 
		}
		
		public boolean endsWord() { 
			return (this.hasChildren() && this.children.containsKey(WORD_ENDING)); 
		}
		
		public boolean isWordEning() { 
			return this.getLetter().equals(WORD_ENDING); 
		}
		
		public boolean isRoot() { 
			return this.getLetter().equals(ROOT_NODE); 
		}
		
		public String toString() { 
			return String.valueOf(this.letter); 
		}
		
		protected DictionaryNode addChild(Character c) {
			DictionaryNode newChild = new DictionaryNode(c, this);
			this.children.put(c, newChild);
			return newChild;
		}
		
		protected String getWordFragment() {
			if(this.distanceFromRoot < 1) {
				return "";
			}
			DictionaryNode currentNode = this;
			char[] letters = new char[this.distanceFromRoot];
			int idx = this.distanceFromRoot - 1;
			while(currentNode.getParent() != null) {
				if(idx < 0) {
					//ERROR - should never happen. Implies distanceFromRoot is not correct
					System.out.println("ERROR: In getWordFragment, distanceFromRoot was not correct.");
					return null;
				}
				letters[idx--] = currentNode.getLetter();
				currentNode = currentNode.getParent();
			}
			return new String(letters);
		}
		
	}
	
	public static void main(String[] args) {
		boolean runTests = false;

		if(runTests) {
			Dictionary.testCases_Scrabble();
			Dictionary.testCases_Simple();
		}
		else {
			Dictionary dictionary = new Dictionary();
			DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","NWL2023.txt"), true);

			DictionaryFilter filter = new DictionaryFilter();
		    filter.addRequiredLetters("LROYTAP");
		    filter.addPossibleLetters("ROYTAP");
		    //filter.wordSizeMin = 4;
		    //filter.wordSizeMax = 4;
		    Dictionary filteredTest = filter.applyTo(dictionary);

		    System.out.println(filteredTest.size);
		    filteredTest.printAll();

			Word wrd = dictionary.getWord("DIM");
			System.out.println(wrd.getLetters() + ": " + wrd.getDefinitions());
			List<Word.Part_Of_Speech> posSet = wrd.getPartsOfSpeech();
			for(Word.Part_Of_Speech pos : posSet) {
				System.out.println(pos);
			}

		}
	}

	/*
	 * Tests on the Scrabble 2023 Dictionary
	 */
	private static void testCases_Scrabble() {
		Dictionary dictionary = new Dictionary();
		DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","NWL2023.txt"), true);
		
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
		
		Dictionary dictionary = new Dictionary();
		DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","small_no_definitions.txt"), false);
		
		// TEST 1 - GUARD appears twice so there are 15 entries but actual size should be 14
		assert (dictionary.getSize() == 14) : "Failed double word in dictionary file test";
		System.out.println(dictionary.getSize());
		
		// TEST 2 - Verify size after adding a word
		assert (dictionary.getWord("BATS") == null) : "BATS is already in the simple dictionary example";
		dictionary.addWord("BATS");
		assert (dictionary.getSize() == 15) : "Failed add word test";
		dictionary.printAll();
		System.out.println(dictionary.getSize());
		System.out.println("-------");
		
		// TEST 3 - Verify size after removing words
		dictionary.removeWord("bat");
		dictionary.removeWord("bats");
		dictionary.printAll();
		System.out.println(dictionary.getSize());
		assert (dictionary.getSize() == 13) : "Failed dictionary size test after word removal";
	}
}