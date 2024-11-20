package com.augursolutions.wordler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.augursolutions.wordler.Word.Part_Of_Speech;

/**
 * Utility class with methods for populating a {@link TreeMapLanguageDictionary} object from external sources
 * 
 * @author Steven Major
 *
 */
public final class DictionaryLoadUtils {

	// private constructor - all methods are static
	private DictionaryLoadUtils() {};
	
	/**
	 * Populate a {@link TreeMapLanguageDictionary} by reading in a text file where each line
	 * contains a word followed by a definition. Intended for use with the 
	 * text file dictionaries in the <b><i>data/words/&lt;region&gt;</i></b> folder
	 * of a <b><i>Zyzzyva</i></b> installation. Any text file with one word per line
	 * can be loaded with this method. Definitions do not need to be present
	 * @param dictionaryFile {@link Path} to the text file with words
	 *        and definitions.
	 * @return A dictionary object with all words  from the specified text file
	 */
	public static final <T extends Dictionary> boolean loadFromZyzzyva(T dictionary, Path dictionaryFile) {	
		File wordList = null;
		try {
			wordList = dictionaryFile.toFile();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if(wordList == null || !wordList.exists()) {
			System.out.println("File '" + dictionaryFile.toString() + "' does not exist or is not readable.");
			return false;
		}
		try(BufferedReader br = new BufferedReader(new FileReader(wordList)); ) {
			String line = null;
			while ((line = br.readLine()) != null)  {  
				String definitionEntry = null;
				List<String> definitions = new ArrayList<>();
				List<Part_Of_Speech> partsOfSpeech = new ArrayList<>();
				// Get the word and definition portions of the entry
				int spaceIndex = line.indexOf(' ');
				// If there is no space then this is just a single word entry, otherwise it is "word the definition of the word"
				// in which case we want to pull apart "word" and "the definition of the word"
				String word = spaceIndex == -1 ? line : line.substring(0, line.indexOf(' '));
				if(spaceIndex > -1 && line.length() > spaceIndex)
					definitionEntry = line.substring(spaceIndex+1);
				// For TreeMapLanguageDictionary, add a Word objectw ith definitions and parts of speech
				if(dictionary instanceof TreeMapLanguageDictionary) {
					if(definitionEntry != null && !definitionEntry.isBlank()) {
						// Definitions have the following format:
						// - homographs (same spelling, different word) are separated by " / "
						// - part of speech occurs at the end of the definition and appears in square brackets, followed by conjugations/pluralization
						// - related words follow the part of speech and are preceded by " : ", e.g. the word "WORK" (verb) might have " : WORKER [n]"
						// e.g. DIM {obscure=adj} [adj DIMMER, DIMMEST] : DIMLY [adv], DIMMISH [adj], DIMNESS [n] / to make dim [v DIMMED, DIMMING, DIMS] : DIMMABLE [adj]
						String[] allDefs = definitionEntry.split(" / ");
						for(String def : allDefs) {
							definitions.add(def);
							partsOfSpeech.add(getPartOfSpeechFromZyzzyva(def));
						}

					}
					((TreeMapLanguageDictionary)dictionary).add(new Word(word,definitions,partsOfSpeech));
				} else {
					dictionary.add(word);
				}
			} 
		} 
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Get the part of speech from a single definition - looks for the first entry in square brackets
	 * @param definition A word definition in the Zyzzyva format
	 * @return {@link Part_Of_Speech}
	 */
	private static final Part_Of_Speech getPartOfSpeechFromZyzzyva(String definition) {
		if(definition == null || definition.isBlank())
			return Part_Of_Speech.UNDEFINED;
		
		// Look for any related word entries and strip them off
		int relatedWordIndex = definition.indexOf(" : ");
		if(relatedWordIndex > -1) {
			definition = definition.substring(0, relatedWordIndex);
		}
		Pattern pattern = Pattern.compile("\\[([a-z]+)\\s?[\\w\\s,]*\\]");
		Matcher matcher = pattern.matcher(definition);
		if(matcher.find()) {
			String partOfSpeech = matcher.group(1);
			switch(partOfSpeech) {
			case "adj":
				return Part_Of_Speech.ADJECTIVE;
			case "adv":
				return Part_Of_Speech.ADVERB;
			case "article":
				return Part_Of_Speech.ARTICLE;
			case "conj":
				return Part_Of_Speech.CONJUNCTION;
			case "interj":
				return Part_Of_Speech.INTERJECTION;
			case "n":
				return Part_Of_Speech.NOUN;
			case "prep":
				return Part_Of_Speech.PREPOSITION;
			case "pron":
				return Part_Of_Speech.PRONOUN;
			case "v" :
				return Part_Of_Speech.VERB;
			default:
				return Part_Of_Speech.UNDEFINED;
			}
		}
		return Part_Of_Speech.UNDEFINED;
	}
}
