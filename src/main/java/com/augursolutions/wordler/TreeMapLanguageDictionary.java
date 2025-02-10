package com.augursolutions.wordler;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Iterator;
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
 * use the {@link HashMapDictionary} class
 * 
 * @author Steven Major
 *
 */
public class TreeMapLanguageDictionary extends TreeMapDictionary implements LanguageDictionary {

	private static final long serialVersionUID = 1L;

	public TreeMapLanguageDictionary() {
		this.rootNode = getRootNode();
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
	 * Add a word to the {@link Dictionary}
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
	 * Wrapper for {@link #add(Word)} for adding words
	 * with no definition
	 * @param word Word (letters only - no definition or parts of speech) to add to the dictionary
	 */
	@Override
	public boolean add(String word) {
		return add(new Word(word));
	}

	@Override
	public Iterator<Word> wordIterator() {
		return new WordIterator();
	}
	
    private class WordIterator implements Iterator<Word> {
    	private Iterator<HashDictionaryNode> iter;
    	public WordIterator() {
    		this.iter = nodeIterator();
    	}
    	
		@Override
		public boolean hasNext() {
			return this.iter.hasNext();
		}

		@Override
		public Word next() {
			return ((LanguageDictionaryNode)this.iter.next()).getWord();
		}
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

	@Override
	public Dictionary clone() {
	    try {
	    	Dictionary d = getClass().getDeclaredConstructor().newInstance();
	    	Iterator<Word> iter = this.wordIterator();
	    	Method addMethod = d.getClass().getMethod("add", Word.class);
	    	while(iter.hasNext()) {
	    		addMethod.invoke(iter.next().clone());
	    	}
	        return d;
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 
	    return null;
	}
	
	public static void main(String[] args) {


		TreeMapLanguageDictionary simple = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(simple,Path.of("./test/dictionaries","small_no_definitions.txt"));
		for(String w : simple) {
			System.out.println(w);
		}
	}
}