package com.augursolutions.wordler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Dictionary implemented as a TreeSet.
 * @author Steven Major
 *
 */
public class TreeSetDictionary extends Dictionary {

	private static final long serialVersionUID = 1L;
	
	private TreeSet<String> allWords;
	
	public TreeSetDictionary() {
		this.allWords = new TreeSet<>();
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

	public Iterator<String> iterator() {
		return this.allWords.iterator();
	}

	public static void main(String[] args) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add("AT");
		strings.add("BED");
		strings.add("BEST");
		strings.add("BE");
		
		TreeSetDictionary d = new TreeSetDictionary();
		d.addAll(strings);
		
		for(String w : d) {
			System.out.println(w);
		}
	}
}
