package com.augursolutions.wordler;

import java.util.HashSet;
import java.util.Iterator;

public class HashSetLanguageDictionary extends Dictionary {

	private static final long serialVersionUID = 1L;

	private HashSet<Word> allWords;
	
	public HashSetLanguageDictionary() {
		this.allWords = new HashSet<>();
	}
	
	@Override
	public Iterator<Word> iterator() {
		return this.allWords.iterator();
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

}
