package com.augursolutions.wordler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class designed to streamline solving the daily <b>NYT Spelling Bee</b> puzzle. Example usage:
 * <pre>
 *		SpellingBeeSolver slvr = new SpellingBeeSolver();
 *		slvr.setDictionary(dictionary);
 *		slvr.setPossibleLetters("PHANTO");
 *		slvr.setRequiredLetter("M");
 *		SpellingBeeSolution sln = slvr.solve();
 *		sln.prettyPrint();
 * </pre>
 * The {@link SpellingBeeSolution#prettyPrint()} method will display all answers in the console, e.g.
 * <pre>
 *PANGRAM(S):
 *	PHANTOM
 *
 *WORDS OF LENGTH 9:
 *	MANHATTAN
 *WORDS OF LENGTH 8:
 *	PHOTOMAP
 *WORDS OF LENGTH 7:
 *	MAHATMA
 *  ...
 * </pre>
 * <p>
 * <b>NOTE:</b> The solutions are correct to the extent that the {@link Dictionary} object
 * used to generate them matches the dictionary used by the <b>NYT Spelling Bee</b>
 * 
 * 
 * @author Steven Major
 *
 */
public class SpellingBeeSolver {
	private static final int N_POSSIBLE_LETTERS = 6;
	private String requiredLetter;
	private String possibleLetters;
	private Dictionary dictionary;
	
	public SpellingBeeSolver() {};
	
	public void setDictionary(Dictionary d) {
	  this.dictionary = d;	
	}
	
	/**
	 * Get the associated {@link Dictionary} object - initializes an empty {@link Dictio9nary} if one has not been set.
	 * @return The {@link Dictionary} object associated with the solver
	 */
	public Dictionary getDictionary() {
		if(this.dictionary == null) {
			this.dictionary = new Dictionary();
		}
		return this.dictionary;
	}
	
	/**
	 * Determine if a non-empty {@link Dictionary has been set
	 * @return {@code true} if the {@link DIctionary} object is not null and not empty
	 */
	public boolean hasValidDictionary() {
		return (this.getDictionary() != null && this.getDictionary().getSize() > 0);
	}
	
	/**
	 * Set the "required" letter in the puzzle (yellow letter in the center). String
	 * should be a single letter (A-Z), e.g. {@code setRequiredLetter("W");}
	 * @param r A single letter (A-Z)
	 */
	public void setRequiredLetter(String r) {
		if(r == null || r.isEmpty() || r.length() != 1) {
			System.out.println("ERROR: setRequiredLetter() takes a String with one character, value provided was: " + r);
			return;
		}
		r = r.toUpperCase();
		Character c = r.charAt(0);
		if(c<'A' || c>'Z') {
			System.out.println("ERROR: setRequiredLetter() takes a String with a letter (A-Z), value provided was: " + r);
			return;
		}
		this.requiredLetter=r;
	}
	
	public String getRequiredLetter( ) {
		return this.requiredLetter;
	}
	
	/**
	 * Determine if the value of {@code requiredLetter} is valid:
	 *  - Not null
	 *  - Contains exactly one letter
	 *  - Letter is in A-Z
	 * @return {@code true} if {@code requiredLetter} is valid.
	 */
	public boolean hasValidRequiredLetter() {
		return (this.requiredLetter != null && 
				this.requiredLetter.length() == 1 &&
				this.requiredLetter.toUpperCase().charAt(0) >= 'A' && 
				this.requiredLetter.toUpperCase().charAt(0) <= 'Z');
	}
	
	public void setPossibleLetters(String p) {
		if(p == null || p.isEmpty() || p.length() != 6) {
			System.out.println("ERROR: setPossibleLetters() takes a String with " + N_POSSIBLE_LETTERS + " characters, value provided was: " + p);
			return;
		}
		p = p.toUpperCase();
		for(int i=0; i<N_POSSIBLE_LETTERS; i++) {
			Character c = p.charAt(i);
			// Verify that this is a letter (A-Z)
			if(c<'A' || c>'Z') {
				System.out.println("ERROR: setPossibleLetters() take a String with letters (A-Z), value provided was: " + c);
				return;
			}
			// Check against duplicates
			if(p.lastIndexOf(c) != i) {
				System.out.println("ERROR: setPossibleLetters() requires " + N_POSSIBLE_LETTERS + " unique letters. The letter '" + c + "' appears twice.");
				return;
			}
		}
		this.possibleLetters = p;
	}
	
	public String getPossibleLetters() {
		return this.possibleLetters;
	}
	
	/**
	 * Verifies that {@code possibleLetters} are valid - 6 unique letters (A-Z)
	 * @return {@code true} if {@code possibleLetters} are valid.
	 */
	public boolean hasValidPossibleLetters() {
		if(this.possibleLetters == null || this.possibleLetters.length() != N_POSSIBLE_LETTERS) {
			return false;
		}
		for(Character c : this.getPossibleLetters().toUpperCase().toCharArray()) {
			// Verify that this is a letter (A-Z)
			if(c < 'A' || c > 'Z') {
				return false;
			}
			// Check against duplicates
			if(this.getPossibleLetters().toUpperCase().lastIndexOf(c) != this.getPossibleLetters().toUpperCase().indexOf(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Verifies that there is a valid {@link Dictionary} object and that {@code requiredLetter} and {@code possibleLetters} 
	 * are set an valid. Then applies a filter to create a new {@link Dictionary} object with words that contain 
	 * {@code requiredLetter} and whose only other letters are in {@code possibleLetters}.
	 * Results are stored in a {@link SpellingBeeSolution} object.
	 * @return A fully populated {@link SpellingBeeSolution} object.
	 */
	public SpellingBeeSolution solve() {
		SpellingBeeSolution sln = new SpellingBeeSolution();
		
		// Check that dictionary, requiredLetter, and possibleLetters are valid
		if(!this.hasValidDictionary()) {
			System.out.println("ERROR: No valid dictioanry to solve Spelling Bee with.");
			return sln;
		}
		if(!this.hasValidRequiredLetter()) {
			System.out.println("ERROR: No valid required letter to solve Spelling Bee with.");
			return sln;
		}
		if(!this.hasValidPossibleLetters()) {
			System.out.println("ERROR: No valid possible letters to solve Spelling Bee with.");
			return sln;
		}
		
		// Create a filter based on requiredLetter and possibleLetters and get all valid words from the dictionary
		DictionaryFilter filter = new DictionaryFilter();
	    filter.addRequiredLetters(this.getRequiredLetter());
	    filter.addPossibleLetters(this.getPossibleLetters());
	    Dictionary spellingBeeAnswers = filter.applyTo(this.getDictionary());
	    
	    // Populate the SpellingBeeSolution object
	    sln.setAllAnswers(spellingBeeAnswers);
	    
	    //For each word in the full solution set, check if it is a pangram (uses all 6 letters) and also store by word length
	    for(Word w : spellingBeeAnswers) {
	    	// Check for pangram
	    	boolean pangram = true;
	    	for(char c : this.getPossibleLetters().toCharArray()) {
	    		if(w.getLetters().indexOf(c) < 0) {
	    			pangram = false;
	    			break;
	    		}
	    	}
	    	if(pangram) {
	    		sln.addPangram(w);
	    	}
	    	// Store word by word length
	    	sln.addAnswerByLength(w);
	    }
	    return sln;
	}
	
	/**
	 * Holds the solution set to a Spelling Bee puzzle. Set of all answers is stored
	 * as a {@link Dictionary} object in {@code allAnsers}. All answers that are pangrams
	 * (i.e. use all letters in the puzzle) are stored as an {@link ArrayList}&lt;{@link Word}&gt; in 
	 * {@code pangrams}. Answers are also organized by word length in {@code answersByLength}
	 * 
	 */
	public static class SpellingBeeSolution {
		private List<Word> pangrams;
		private Map<Integer, ArrayList<Word>> answersByLength;
		private Dictionary allAnswers;
		
		public SpellingBeeSolution() {
			this.pangrams = new ArrayList<>();
			this.answersByLength = this.getAnswersByLength();
		}
		
		public SpellingBeeSolution(Dictionary d) {
			this();
			this.setAllAnswers(d);
		}
		
		public void setAllAnswers(Dictionary d) {
			this.allAnswers = d;
		}
		
		public Dictionary getAllAnswers() {
			if(this.allAnswers == null) {
				this.allAnswers = new Dictionary();
			}
			return this.allAnswers;
		}
		
		public void addPangram(Word w) {
			this.getPangrams().add(w);
		}
		
		public List<Word> getPangrams() {
			if(this.pangrams == null) {
				this.pangrams = new ArrayList<>();
			}
			return this.pangrams;
		}
		
		/**
		 * Takes a {@link Word} object and stores it in {@code answersByLength} based on its length
		 * @param w {@link Word} to store
		 */
		public void addAnswerByLength(Word w) {
			if(w == null || w.toString().isBlank())
				return;
			Integer l = w.getLetters().length();
			if(!this.getAnswersByLength().containsKey(l)) {
				this.getAnswersByLength().put(l, new ArrayList<>());
			}
			this.getAnswersByLength().get(l).add(w);
		}
		
		/**
		 * Returns a {@link TreeMap}&lt;{@link Integer}, {@link ArrayList}&lt;{@link Word}&gt;&gt; that allows
		 * solutions to be retrieved based on word length. 
		 * @return {@link TreeMap} from {@link Integer} to an {@link ArrayList} of all {@link Word} objects with that length that are 
		 * valid answers
		 */
		public Map<Integer, ArrayList<Word>> getAnswersByLength() {
			if(this.answersByLength == null) {
				// Store answers by length in decreasing order - i.e.. reverse default sorting for Integers
				this.answersByLength = new TreeMap<>(new Comparator<Integer>() {
				    public int compare(Integer x, Integer y) {
				        return Integer.compare(y, x);
				    }
				});
			}
			return this.answersByLength;
		}
		
		/**
		 * Displays all of the solution words in the console - first any Pangrams and then 
		 * all solution words sorted by word length (largest to smallest), e.g.
		 * <pre>
		 * PANGRAM(S):
		 * 	PHANTOM
		 * 
		 * WORDS OF LENGTH 9:
		 * 	MANHATTAN
		 * 	OOMPAHPAH
		 * WORDS OF LENGTH 8:
		 * 	PHOTOMAP
		 * WORDS OF LENGTH 7:
		 * 	MAHATMA
		 * 	MAMMOTH
		 * 	OTTOMAN
		 * 	PHANTOM
		 * 	POMPANO
		 * WORDS OF LENGTH 6:
		 * 	AMMONO
		 * 	ATAMAN
		 * 	HAMMAM
		 * ...
		 * </pre>
		 */
		public void prettyPrint() {
			System.out.println("PANGRAM(S):");
			if(this.getPangrams().isEmpty()) {
				System.out.println("\tNONE");
			} else {
				for(Word p : this.getPangrams()) {
					System.out.println("\t"+p);
				}
			}
			System.out.println("");
			if(this.getAnswersByLength().isEmpty()) {
				System.out.println("NO ANSWERS FOUND");
			}
			for (Map.Entry<Integer, ArrayList<Word>> entry : this.getAnswersByLength().entrySet()) {
				System.out.println("WORDS OF LENGTH " + entry.getKey() + ":");
				for(Word w : entry.getValue()) {
					System.out.println("\t"+w);
				}
			}
		}
	}

	public static void main(String[] args) {
		Dictionary dictionary = new Dictionary();
		DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of("./test/dictionaries","NWL2023.txt"), true);
		SpellingBeeSolver slvr = new SpellingBeeSolver();
		slvr.setDictionary(dictionary);
		slvr.setPossibleLetters("PHANTO");
		slvr.setRequiredLetter("M");
		SpellingBeeSolution sln = slvr.solve();
		sln.prettyPrint();
	}
}