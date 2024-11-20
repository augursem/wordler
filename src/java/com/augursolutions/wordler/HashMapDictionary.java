package com.augursolutions.wordler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *  
 * Dictionary implemented as an M-ary tree. Any ASCII character may be stored in the Dictionary with the exception of the
 * `start of text` and `end of text` characters (02 and 03) as these are used to indicate the root node and word endings
 * respectively. Null and empty strings are not supported as entries.
 * <p>
 * Each node in the tree is one "letter" in one or more words, with the ASCII `start of text` value (02)
 * used for the root node and the `end of text` value (03) used for nodes that indicate the end of a word. 
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
 * 
 * @author Steven Major
 *
 */
public class HashMapDictionary extends Dictionary {

	private static final long serialVersionUID = 1L;
	protected HashDictionaryNode rootNode;
	protected int size;
	
	public HashMapDictionary() {
		this.rootNode = getRootNode();
		this.size = 0;
	}

	@Override
	public int getSize() {
		return this.size;
	}
	public HashDictionaryNode getRootNode() {
		if(this.rootNode == null) {
			this.rootNode = new HashDictionaryNode();
		}
		return this.rootNode;
	}

	/**
	 * Add a word to the dictionary
	 * @param word Word to add
	 * @return The {@link #DictionaryNode} that ends the added word (null if {@code word} is null or empty)
	 */
	public boolean add(String word) {
		if(word == null || word.isEmpty()) {
			return false;
		}
		HashDictionaryNode currentNode = this.getRootNode();
		for(int i=0; i<word.length(); i++) {
			Character c = Character.valueOf(word.charAt(i));
			// If char doesn't exist in the tree, add it
			if(!currentNode.getChildren().containsKey(c)) {
				currentNode = currentNode.addChild(c);
			} else {
				currentNode = currentNode.getChildren().get(c);
			}
		}
		// At the end of the word, add additional WORD_ENDING node
		if(!currentNode.endsWord()) {
			currentNode.addWordEnding();
			this.size++;
			return true;
		}
		// Word already exists
		return false;
	}

	/**
	 * Remove a word from the {@link Dictionary}; the {@link Dictionary} object is 
	 * modified in the process. If {@code word} is not present in the dictionary, it is
	 * left unmodified
	 * @param word Word to be removed from the {@link Dictionary}
	 */
	public void remove(String word) {

		if(word == null || word.isEmpty())
			return;

		// Get the node of the last letter in the word 
		HashDictionaryNode node = this.getNode(word);
		// If it is null or does not end a word, then the word is not in the dictionary
		if(node == null || !node.endsWord()) {
			return;
		}
		
		// Remove the WORD_ENDING node, the navigate up the tree removing nodes belonging to this word until
		// we get to the root node or, after removing a letter, the node we are on has other childrens
		Character charToRemove = node.getWordEnding().getLetter();
		while(true) {
			node.getChildren().remove(charToRemove);
			if(node.hasChildren() || node.isRoot()) {
				break;
			}
			charToRemove = node.getLetter();
			node = node.getParent();
		}
		this.size--;
	}
	
	/**
	 * Determine if a word is in the dictionary
	 * @param word String to check against the dictionary
	 * @return {@code true} if each character in the string is in the dictionary AND the last character has a 
	 * WORD_ENDING node as a child
	 */
	public boolean contains(String word) {
		if(word == null || word.isEmpty()) {
			return false;
		}
		HashDictionaryNode currentNode = this.getRootNode();
		for(int i=0; i<word.length(); i++) {
			Character c = Character.valueOf(word.charAt(i));
			currentNode = currentNode.getChildren().get(c);
			// If char doesn't exist in the tree, then word isn't in the dictionary
			if(currentNode == null)
				return false;
		}
		// At the end of the word, check for WORD_ENDING node child
		return currentNode.endsWord();
	}

	@Override
	public Word getWord(String s) {
	   if(!this.contains(s))
		   return null;
	   return new Word(s);
	}
    
	/**
	 * Get the {@link HashDictionaryNode} associated with a word fragment. For example, if the word "DOG" is in the
	 * {@link Dictionary}, then {@code getNode("DO")} will return the node whose letter is "O" with parent "D" and child "G"
	 * (other children may be present)
	 * @param wordFragment partial word (can be full word) to fetch the node for. 
	 * @return Corresponding node of the last letter in {@code wordFragment}. NULL if the word fragment is not in the dictionary.
	 */
	public HashDictionaryNode getNode(String wordFragment) {
		if(wordFragment == null || wordFragment.isEmpty()) {
			return null;
		}
		HashDictionaryNode node = this.getRootNode();
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
	 * Print all words to the console (uses recursion - large dictionaries may result in many calls being added to the stack)
	 */
	public void printAll() {
		this.printAll(this.getRootNode());
	}
	private void printAll(HashDictionaryNode node) {
		// Catch bad inputs / malformed dictionary node
		if(node == null || node.letter == null || node.children == null || node.children.isEmpty()) {
			return;
		}
		for(Map.Entry<Character, HashDictionaryNode> entry : node.children.entrySet()) {
			HashDictionaryNode childNode = entry.getValue();
			if(childNode.isWordEning())
				System.out.println(childNode.printChildren());
			else
				printAll(childNode);
		}
	}

	/**
	 * Single node within the dictionary tree
	 * @author Steven Major
	 */
	public static class HashDictionaryNode implements Serializable {

		private static final long serialVersionUID = 1L;
		protected static final char ROOT_NODE   = (char)02;
		protected static final char WORD_ENDING = (char)03;
		
		protected Character letter;
		protected Map<Character, HashDictionaryNode> children;
		protected HashDictionaryNode parent;
		protected int distanceFromRoot = 0;
		
		/** 
		 * Constructor for new root node
		 */
		protected HashDictionaryNode() {
			this(ROOT_NODE);
		};
		
		protected HashDictionaryNode(Character c) {
			this.setLetter(c);
			this.setChildren(this.initializeChildren());
		}
		
		protected void setDistanceFromRoot(int d) {
			this.distanceFromRoot = d;
		}
		
		public int getDistanceFromRoot() {
			return this.distanceFromRoot;
		}
		
		public Character getLetter() {
			return this.letter;
		}
		
		public void setLetter(Character c) {
			this.letter = c;
		}
		
		public HashDictionaryNode getParent() {
			return this.parent;
		}
		
		public void setParent(HashDictionaryNode p) {
			this.parent = p;
		}
		 
		public Map<Character, HashDictionaryNode> getChildren() {
			return this.children;
		}
		
		public void setChildren(Map<Character, HashDictionaryNode> children) {
		  this.children = children;	
		}
		
		public boolean hasChildren() { 
			return (this.children != null && !this.getChildren().isEmpty()); 
		}
		
		protected Map<Character, HashDictionaryNode> initializeChildren() {
			return new HashMap<>();
		}
		
		/**
		 * Determine if this node is the last letter of a word in the {@link Dictionary}
		 * @return {@code true if this node has a {@code WORD_ENDING} child}
		 */
		public boolean endsWord() { 
			return (this.hasChildren() && this.getChildren().containsKey(WORD_ENDING)); 
		}
		
		/**
		 * Determine if this node is a WORD_ENDING node
		 * @return {@code true} if this node's character is the {@code WORD_ENDING}
		 */
		public boolean isWordEning() { 
			return this.getLetter().equals(WORD_ENDING); 
		}
		
		/**
		 * Retrieve the WORD_ENDING node that is the child of this node.
		 * It is assumed that such a node exists - should call {@linl #endsWord()}
		 * before calling.
		 * @return The {@code WORD_ENDING} {@link HashDictionaryNode} that is a child of this node 
		 */
		public HashDictionaryNode getWordEnding() {
			return this.getChildren().get(WORD_ENDING);
		}
		
		public boolean isRoot() { 
			return this.getLetter().equals(ROOT_NODE); 
		}
		
		public String toString() { 
			return String.valueOf(this.getLetter()); 
		}
		
		/**
		 * Adds a new child node to the {@link DIctionaryNode} with
		 * this node as its parent.
		 * @param c character to associate with the new child
		 * @return The child node that was added.
		 */
		protected HashDictionaryNode addChild(Character c) {
			HashDictionaryNode newChild = new HashDictionaryNode(c);
			newChild.setParent(this);
			newChild.setDistanceFromRoot(this.getDistanceFromRoot() + 1);
			this.getChildren().put(c, newChild);
			return newChild;
		}
		
		/**
		 * Add a WORD_ENDING node to this node's children
		 * @return The child node that was added
		 */
		protected HashDictionaryNode addWordEnding() {
			return addChild(WORD_ENDING);
		}
		
		/**
		 * Get all letters represented by this node and its children
		 * @return The "word fragment" represented by this node in the tree
		 */
		protected String getWordFragment() {
			if(this.isRoot()) {
				return "";
			}
			HashDictionaryNode currentNode = this;
			char[] letters = new char[this.getDistanceFromRoot()];
			int idx = this.getDistanceFromRoot() - 1;
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
		
		protected String printChildren() {
			if(this.distanceFromRoot < 2) {
				return "";
			}
			HashDictionaryNode currentNode = this.parent;
			char[] letters = new char[this.distanceFromRoot - 1];
			int idx = this.distanceFromRoot - 1;
			while(currentNode.parent != null) {
				if(idx-- < 0) {
					//ERROR
					return null;
				}
				letters[idx] = currentNode.letter;
				currentNode = currentNode.parent;
			}
			return new String(letters);
		}
		
	}


    public Iterator<HashDictionaryNode> nodeIterator() {
        return new NodeIterator();
    }
    
    private class NodeIterator implements Iterator<HashDictionaryNode> {
    	private HashDictionaryNode currentNode;
    	private HashDictionaryNode nextChild;
    	
    	public NodeIterator() {
    		currentNode = (HashDictionaryNode)getRootNode();
    		if(currentNode.hasChildren())
    			nextChild = getFirstChild();
    		else
    			nextChild = null;
    	}
    	
    	/**
    	 * Returns {@code true} if the iteration has more elements.
    	 * <p>
    	 * If currentNode is a WORD_ENDING node, advances currentNode
    	 * to the next node with children. Subsequent calls will not advance currentNode further.
    	 * Calling next() will advance currentNode to the next WORD_ENDING node and return the associated string.
    	 * 
    	 * @return {@code true} if the iteration has more elements
    	 */
    	public boolean hasNext() {
    		/*
    		 * When iterating over letters in the tree, WORD_ENDING nodes are retrieved first amongst all
    		 * siblings, and every node that is not a WORD_ENDING node will have at least one child (a
    		 * WORD_ENDING node if it ends a word and/or letter node(s) if part of another word).
    		 * 
    		 * When next() is called, currentNode is set to a WORD_ENDING node. When hasNext() is called, there
    		 * are two cases to consider:
    		 *   1) next() was the last call made
    		 *   2) Iterator was just instantiated or hasNext() was the last call made
    		 *   
    		 * For case 1, we move backwards through the tree looking for the next node with children. Once we find one, we stop
    		 * For case 2, we should already be at a node with children. If we are not, then this is an empty dictionary. In either
    		 * case, we are just checking to see if there is a nextChild.
    		 */
    		// CASE 1 - we are at a WORD_ENDING node. navigate to next node with a child and set nextChild in the process
    		if(currentNode.isWordEning()) {
    			Character previousLetter;
    			while(true) {
    				previousLetter = currentNode.getLetter();
    				if(!navigateUpTree()) {
    					// Happens if we are got up the root and there was no next child to iterate on
    					// i.e. we went up to root, navigateToNextChild() returned false, and we are now
    					// calling navigateUpTree() on the root node
    					return false;
    				}
    				nextChild = getNextChild(previousLetter);
    				if(nextChild != null) {
    					return true;
    				}
    			}
    		}
    		// CASE 2 - check if there is a valid nextChild value.
    		return (nextChild != null);

    	}
    	
    	public HashDictionaryNode next() throws NoSuchElementException {
    		if(!hasNext()) {
    			throw new NoSuchElementException();
    		}
    		// hasNext() returned true, so nextChild is valid
    		// WORD_ENDING will be the first child if present, so keep getting the
    		// first child until a WORD_ENDING is reached.
    		while(true) {
    			currentNode = nextChild;
    			if(currentNode.isWordEning()) {
    				return currentNode;
    			}
    			nextChild = getFirstChild();
    		}
    	}
    	
    	/**
    	 * Move currentNode up (closer to ROOT_NODE) one level
    	 */
    	private boolean navigateUpTree() {
    		if(currentNode.isRoot())
    			return false;
    		currentNode = (HashDictionaryNode)currentNode.getParent();
    		return true;
    	}
    	
    	/**
    	 * Given a previously navigated to child, get the next child
    	 * @param previousLetter - letter of child that was previously navigated to from this node
    	 * @return Next node to navigate to - null if there are no more children to navigate to
    	 */
    	private HashDictionaryNode getNextChild(Character previousLetter) {
    		boolean reachedPreviuousLetter = false;
    		for (Map.Entry<Character, HashDictionaryNode> entry : currentNode.getChildren().entrySet()) {
    			// If the previous iteration through this loop found the 'previousLetter' value, then
    			// this iteration corresponds to the 'next' value
    			if(reachedPreviuousLetter) {
    				return entry.getValue();
    			}
    			if(entry.getKey().equals(previousLetter)) {
    				reachedPreviuousLetter = true;
    			}
    		}
    		// Get here if we reachedPreviuousLetter was set to true on the last iteration through the loop - e.g. no more letters to go through
    		return null;
    	}
    	
    	/**
    	 * Get the 'first' child node of this node
    	 * @return The first child node of this node, null if there are nbo children
    	 */
    	private HashDictionaryNode getFirstChild() {
    		// Always return WORD_ENDING first if it exists
    		if(currentNode.endsWord())
    			return currentNode.getWordEnding();
    		// Otherwise return the first child that the iterator fetches
    		for(HashDictionaryNode n : currentNode.getChildren().values()) {
    			return n;
    		}
    		// Only get here if we didn't check `hasNext()` result before calling this method
    		return null;
    	}
    }

	@Override
	public Iterator<Word> iterator() {
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
			return new Word(this.iter.next().getParent().getWordFragment());
		}
    }
    
	public static void main(String[] args) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("AT");
		strings.add("BED");
		strings.add("BEST");
		strings.add("BE");
		
		HashMapDictionary d = new HashMapDictionary();
		d.addAll(strings);
		
		//d.printAll();
		
		Map<Integer, Dictionary> dMap = new HashMap<>();
		dMap.put(1, d);
		((HashMapDictionary)dMap.get(1)).printAll();
		
	}

}
