package com.augursolutions.wordler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * Dictionary implemented as an M-ary tree. Any ASCII character may be stored in the Dictionary with the exception of the
 * `start of text` and `end of text` characters (02 and 03) as these are used to indicate the root node and word endings
 * respectively.
 * <p>
 * Each node in the tree is one "letter" in one or more words, with the ASCII `start of text` value (02)
 * used for the root node and the `end of text` value (03) used for nodes that indicate the end of a word. 
 * The children for each node are stored in a {@link TreeMap} so that values are retrieved in alphabetical order (if all
 * values are alpha-numeric). Actual ordering is based on the ASCII integer value of each char.
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
public class SimpleDictionary {
	
	/**
	 * Single node within the dictionary tree
	 * @author Steven Major
	 */
	private class DictionaryNode {
		
		protected Character letter;
		protected Map<Character, DictionaryNode> children;
		protected DictionaryNode parent;
		protected int distanceFromRoot = 0;
		
		protected DictionaryNode(Character c) {
			this.letter = c;
			this.children = new HashMap<>();
			this.parent = null;
		}
		
		protected DictionaryNode addChild(Character c) {
			DictionaryNode newChild = new DictionaryNode(c);
			newChild.parent = this;
			newChild.distanceFromRoot = this.distanceFromRoot + 1;
			this.children.put(c, newChild);
			return newChild;
		}
		
		protected String printChildren() {
			if(this.distanceFromRoot < 2) {
				return "";
			}
			DictionaryNode currentNode = this.parent;
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
		
		public String toString() { return String.valueOf(this.letter); }
		
	}
	
	private static final char ROOT_NODE   = (char)02;
	private static final char WORD_ENDING = (char)03;
	
	private DictionaryNode rootNode;
	
	public SimpleDictionary( ) {
		this.rootNode = new DictionaryNode(ROOT_NODE);
	}
	
	/**
	 * Add a word to the dictionary
	 * @param word Word to add
	 * @return true If the word is added successfully (false if it already exists or if {@code word} is null or empty)
	 */
	public boolean addWord(String word) {
		if(word == null || word.isEmpty()) {
			return false;
		}
		DictionaryNode currentNode = this.rootNode;
		for(int i=0; i<word.length(); i++) {
			Character c = Character.valueOf(word.charAt(i));
			// If char doesn't exist in the tree, add it
			if(!currentNode.children.containsKey(c)) {
				currentNode = currentNode.addChild(c);
			} else {
				currentNode = currentNode.children.get(c);
			}
		}
		// At the end of the word, add additional WORD_ENDING node
		if(!currentNode.children.containsKey(WORD_ENDING)) {
			currentNode.addChild(WORD_ENDING);
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
	public void removeWord(String word) {

		if(word == null || word.isEmpty())
			return;
		
		// First collect a list of nodes corresponding to each letter in the word
		LinkedList<DictionaryNode> nodes = new LinkedList<>();
		DictionaryNode currentNode = this.rootNode;
		for (int i = 0; i < word.length(); i++){
		    Character c = Character.valueOf(word.charAt(i));
		    // Return if the word is not in the dictionary
		    if(!currentNode.children.containsKey(c)) {
		    	return;
		    }
		    currentNode = currentNode.children.get(c);
    		nodes.push(currentNode);
		}
		
		//If we are at the end of the list and this is not the end of some word, return
		if(!currentNode.children.containsKey(WORD_ENDING)) {
	    	return;
		}
		
		// Now, go through the list of nodes from end to start. At each one, remove
		// the corresponding character from its children (starting with removing WORD_ENDING from
		// the node corresponding to the last letter). If after doing this that node has no children,
		// i.e. no other words are using this node, move on to the next node and remove the now
		// childless node from it's children. Repeat until all letters are removed or until we reach a 
		// node that still has children after removing a letter.
		Character currentChar = WORD_ENDING;
		while(!nodes.isEmpty()) {
			currentNode = nodes.pop();
			currentNode.children.remove(currentChar);
			// If there are other child nodes, then this node is part of another word - leave it (stop)
			if(currentNode.children.isEmpty()) {
				break;
			}
			currentChar = currentNode.letter;
		}
	}

	/**
	 * Print all words to the console
	 */
	public void printAll() {
		this.printAll(this.rootNode);
	}
	private void printAll(DictionaryNode node) {
		// Catch bad inputs / malformed dictionary node
		if(node == null || node.letter == null || node.children == null || node.children.isEmpty()) {
			return;
		}
		for(Map.Entry<Character, DictionaryNode> entry : node.children.entrySet()) {
			DictionaryNode childNode = entry.getValue();
			if(childNode.letter == WORD_ENDING)
				System.out.println(childNode.printChildren());
			else
				printAll(childNode);
		}
	}
	
	public static void main(String[] args) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("AT");
		strings.add("BED");
		strings.add("BEST");
		strings.add("BE");
		
		SimpleDictionary d = new SimpleDictionary();
		for(String s : strings) {
			d.addWord(s);
		}
		
		d.printAll();
		
	}

}
