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
	
	/**
	 * Assigns the {@link Dictionary} that will be used to generate all solutions
	 * @param d A {@link Dictionary} with all words to get solutions from ({@code allAnswers} will be a subset of this dictionary)
	 */
	public void setDictionary(Dictionary d) {
	  this.dictionary = d;	
	}
	
	/**
	 * Get the associated {@link Dictionary} object - initializes an empty {@link Dictionary} if one has not been set.
	 * @return The {@link Dictionary} object associated with the solver
	 */
	public Dictionary getDictionary() {
		if(this.dictionary == null) {
			this.dictionary = new Dictionary();
		}
		return this.dictionary;
	}
	
	/**
	 * Determine if a non-empty {@link Dictionary} has been set
	 * @return {@code true} if the {@link Dictionary} object is not null and not empty
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
	
	/**
	 * Get the {@code requiredLetter} field
	 * @return The {@code requiredLetter} field
	 */
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
	
	/**
	 * @param p A {@link String} with 6 unique characters - performs validation on the input.
	 */
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
	
	/**
	 * @return The {@code possibleLetters} field - may be null
	 */
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
		
		/**
		 * Initialize an empty SpellingBeeSolution object
		 */
		public SpellingBeeSolution() {
			this.allAnswers = new Dictionary();
			this.pangrams = new ArrayList<>();
			this.answersByLength = this.getAnswersByLength();
		}
		
		/**
		 * Assign a {@link Dictionary} object as the set of all answers
		 * @param d {@link Dictionary} object with answers
		 */
		public void setAllAnswers(Dictionary d) {
			this.allAnswers = d;
		}
		
		/**
		 * Returns the {@link Dictionary} object with all answers
		 * @return {@link Dictionary} object with all answers
		 */
		public Dictionary getAllAnswers() {
			if(this.allAnswers == null) {
				this.allAnswers = new Dictionary();
			}
			return this.allAnswers;
		}
		
		/**
		 * Add a {@link Word} to the list of pangrams. Note that there is no check here 
		 * that the word is included in the {@code allAnswers} dictionary
		 * @param w {@link Word} to add to the list of pangrams
		 */
		public void addPangram(Word w) {
			this.getPangrams().add(w);
		}
		
		/**
		 * Return the {@code pangrams} object - initializes the list if it is null
		 * @return The {@code pangrams} field - {@link ArrayList}&lt;{@link Word}&gt; of solutions that use all letters
		 */
		public List<Word> getPangrams() {
			if(this.pangrams == null) {
				this.pangrams = new ArrayList<>();
			}
			return this.pangrams;
		}
		
		/**
		 * Takes a {@link Word} object and stores it in {@code answersByLength} based on its length
		 * Note that there is no check that the word is included in the {@code allAnswers} dictionary
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

	/**
	 * Generate solutions to a NYT Spelling Bee puzzle
	 * @param args: <br>
	 *   1: requiredLetter - the single yellow letter that all solutions must use
	 *   2: possibleLetters - the six white letters that solutions may use
	 *   3: dictionary to use, default is "./test/dictionaries/NWL2023.txt"
	 */
	public static void main(String[] args) {
		// Default dictionary
		String dictionarypath = "./test/dictionaries/NWL2023.txt";
		String requiredLetter = "";
		String possibleLetters = "";
		if(args.length < 2) {
			System.out.println("ERROR: at least two arguments are required: requiredLetter and possibleLetters.");
			return;
		}
		requiredLetter = args[0];
		possibleLetters = args[1];
		if(args.length == 3) {
			dictionarypath = args[2];
		}
		if(args.length > 3) {
			System.out.println("WARNING: Only 3 arguments are accepted - additional arguments are ignored.");
		}
		Dictionary dictionary = new Dictionary();
		DictionaryLoadUtils.loadFromZyzzyva(dictionary,Path.of(dictionarypath), true);
		SpellingBeeSolver slvr = new SpellingBeeSolver();
		slvr.setDictionary(dictionary);
		slvr.setPossibleLetters(possibleLetters);
		slvr.setRequiredLetter(requiredLetter);
		SpellingBeeSolution sln = slvr.solve();
		sln.prettyPrint();
	}
}