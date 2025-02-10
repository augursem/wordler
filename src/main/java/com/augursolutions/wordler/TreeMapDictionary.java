package com.augursolutions.wordler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.HashMap;

/**
 * Implementation of {@link Dictionary} that is very similar to {@link HashMapDictionary} except
 * that child nodes are stored in a {@link TreeMap} instead of a {@link HashMap}. There is some performance
 * trade-off, but in exchange, elements are sorted alphabetically (according to each character's int value)
 * and the dictionary is {@link Iterable}
 * @author Steven Major
 *
 */
public class TreeMapDictionary extends HashMapDictionary {

	private static final long serialVersionUID = 1L;
	
	public TreeMapDictionary() {
		this.rootNode = getRootNode();
	}
	
	public HashDictionaryNode getRootNode() {
		if(this.rootNode == null) {
			this.rootNode = new TreeDictionaryNode();
		}
		return this.rootNode;
	}
	
	public void printAll() {

		for(String w : this) {
			System.out.println(w);	
		}
	}
	
	/**
	 * Single node within the dictionary tree that stores children in a {@link TreeMap}
	 * @author Steven Major
	 */
	public static class TreeDictionaryNode extends HashMapDictionary.HashDictionaryNode{

		private static final long serialVersionUID = 1L;
		
		protected TreeDictionaryNode() {
			this(HashDictionaryNode.ROOT_NODE);
		}
		
		protected TreeDictionaryNode(Character c) {
			this.setLetter(c);
			this.setChildren(this.initializeChildren());
		}
		
		@Override
		protected Map<Character, HashDictionaryNode> initializeChildren() {
			return new TreeMap<>();
		}
		
		@Override
		protected HashDictionaryNode addChild(Character c) {
			TreeDictionaryNode newChild = new TreeDictionaryNode(c);
			newChild.setParent(this);
			newChild.setDistanceFromRoot(this.getDistanceFromRoot() + 1);
			this.getChildren().put(c, newChild);
			return newChild;
		}
		 
		@Override
		public NavigableMap<Character, HashDictionaryNode> getChildren() {
			return (TreeMap<Character, HashDictionaryNode>)this.children;
		}
		
	}

    @Override
    public Iterator<HashDictionaryNode> nodeIterator() {
        return new TreeNodeIterator();
    }
    
    private class TreeNodeIterator implements Iterator<HashDictionaryNode> {
    	private TreeDictionaryNode currentNode;
    	private TreeDictionaryNode nextChild;
    	
    	public TreeNodeIterator() {
    		currentNode = (TreeDictionaryNode)getRootNode();
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
    	
    	public TreeDictionaryNode next() throws NoSuchElementException {
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
    		currentNode = (TreeDictionaryNode)currentNode.getParent();
    		return true;
    	}
    	
    	/**
    	 * Given a previously navigated to child, get the next child
    	 * @param previousLetter - letter of child that was previously navigated to from this node
    	 * @return Next node to navigate to - null if there are no more children to navigate to
    	 */
    	private TreeDictionaryNode getNextChild(Character previousLetter) {
    		Map.Entry<Character, HashDictionaryNode> next = currentNode.getChildren().higherEntry(previousLetter);
    		if(next == null)
    			return null;
    		return (TreeDictionaryNode)next.getValue();
    	}
    	
    	/**
    	 * Get the 'first' child node of this node
    	 * @return The first child node of this node, null if there are no children
    	 */
    	private TreeDictionaryNode getFirstChild() {
    		return (TreeDictionaryNode)currentNode.getChildren().firstEntry().getValue();
    	}
    }
    
	public static void main(String[] args) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("AT");
		strings.add("BED");
		strings.add("BEST");
		strings.add("BE");
		
		TreeMapDictionary d = new TreeMapDictionary();
		d.addAll(strings);
		
		d.printAll();
	}
}
