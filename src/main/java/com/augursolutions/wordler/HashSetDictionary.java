package com.augursolutions.wordler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.HashSet;

/**
 * Dictionary implemented as a HashSet.
 * @author Steven Major
 *
 */
public class HashSetDictionary extends Dictionary {

	private static final long serialVersionUID = 1L;
	
	private HashSet<String> allWords;
	
	public HashSetDictionary() {
		this.allWords = new HashSet<>();
	}
	
	@Override
	public int getSize() {
		return this.allWords.size();
	}
	
	@Override
	public boolean add(String word) {
		if(word == null || word.isEmpty()) {
			return false;
		}
		this.allWords.add(word);
		return true;
	}

	@Override
	public void remove(String word) {
		this.allWords.remove(word);
	}

	@Override
	public boolean contains(String word) {
		return this.allWords.contains(word);
	}

	@Override
	public Word getWord(String s) {
	   if(!this.contains(s))
		   return null;
	   return new Word(s);
	}
	
	public Iterator<String> stringIterator() {
		return this.allWords.iterator();
	}
    
	@Override
	public Iterator<Word> iterator() {
		return new WordIterator();
	}
    private class WordIterator implements Iterator<Word> {
    	private Iterator<String> iter;
    	public WordIterator() {
    		this.iter = stringIterator();
    	}
    	
		@Override
		public boolean hasNext() {
			return this.iter.hasNext();
		}

		@Override
		public Word next() {
			return new Word(this.iter.next());
		}
    }
    
	public static void main(String[] args) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("AT");
		strings.add("BED");
		strings.add("BEST");
		strings.add("BE");
		
		TreeSetDictionary d = new TreeSetDictionary();
		d.addAll(strings);
		
		for(Word w : d) {
			System.out.println(w);
		}
	}
}
