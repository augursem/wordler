package com.augursolutions.wordler;

import java.util.ArrayList;
import java.util.List;

public class Word {

	public enum Part_Of_Speech {
		ACRONYM,
		ADJECTIVE,
		ADVERB,
		ARTICLE,
		CONJUNCTION,
		INTERJECTION,
		NOUN,
		PREPOSITION,
		PRONOUN,
		PROPER_NOUN,
		VERB,
		UNDEFINED;
	}
	
	private String letters = null;
	private List<String> definitions = new ArrayList<>();
	private List<Part_Of_Speech> partsOfSpeech = new ArrayList<>();
	
	public Word(){}
	
	public Word(String word) {
		this(word,null,null);
	}
	
	public Word(String word, String definition) {
		this(word,null,null);
		this.definitions.add(definition);
	}
	
	public Word(String letters, List<String> definitions, List<Part_Of_Speech> partsOfSpeech) {
		this.letters = letters.toUpperCase();
		if(definitions != null)
		  this.definitions = definitions;
		if(partsOfSpeech == null)
			this.partsOfSpeech.add(Part_Of_Speech.UNDEFINED);
		else
			this.partsOfSpeech = partsOfSpeech;
	}
	
	//////////////////////////////////////
	//       Setters / Getters          //
	//////////////////////////////////////
	
	// WORD
	public String getLetters() {
		return this.letters;
	}
	public void setLetters(String letters) {
		this.letters = letters;
	}
	// DEFINITION
	public List<String> getDefinitions() {
		return this.definitions;
	}
	public void setDefinitions(List<String> definitions) {
		this.definitions = definitions;
	}
	public boolean hasDefintiions( ) {
		return this.definitions != null && !this.definitions.isEmpty();
	}
	//PART OF SPEECH
	public List<Part_Of_Speech> getPartsOfSpeech() {
		return this.partsOfSpeech;
	}
	public void setPartsOfSpeech(List<Part_Of_Speech> partsOfSpeech) {
		this.partsOfSpeech = partsOfSpeech;
	}
	//CONVERSION TO CHAR[]
	public char[] toChars() {
		if(this.letters == null || this.letters.isEmpty())
			return new char[0];
		char[] chars = new char[this.letters.length()];
		this.letters.getChars(0, this.letters.length(), chars, 0);
		return chars;
	}
	public int length() {
		if(this.letters == null)
			return 0;
		return this.letters.length();
	}
}