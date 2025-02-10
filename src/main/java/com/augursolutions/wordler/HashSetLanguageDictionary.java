package com.augursolutions.wordler;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;

public class HashSetLanguageDictionary extends Dictionary implements LanguageDictionary {

	private static final long serialVersionUID = 1L;

	private HashSet<Word> allWords;
	
	public HashSetLanguageDictionary() {
		this.allWords = new HashSet<>();
	}
	
	@Override
	public Iterator<Word> wordIterator() {
		return this.allWords.iterator();
	}

	@Override
	public Iterator<String> iterator() {
		return new WordIterator();
	}
    private class WordIterator implements Iterator<String> {
    	private Iterator<Word> iter;
    	public WordIterator() {
    		this.iter = wordIterator();
    	}
    	
		@Override
		public boolean hasNext() {
			return this.iter.hasNext();
		}

		@Override
		public String next() {
			return this.iter.next().toString();
		}
    }
	
	@Override
	public int getSize() {
		return this.allWords.size();
	}

	@Override
	public boolean add(String word) {
		return this.allWords.add(new Word(word));
	}
	public boolean add(Word word) {
		return this.allWords.add(word);
	}

	@Override
	public void remove(String word) {
		this.allWords.remove(new Word(word));
	}
	public void remove(Word word) {
		this.allWords.remove(word);
	}

	@Override
	public boolean contains(String word) {
		return this.allWords.contains(new Word(word));
	}

	@Override
	public Word getWord(String s) {
	   if(!this.contains(s))
		   return null;
	   return new Word(s);
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
}
