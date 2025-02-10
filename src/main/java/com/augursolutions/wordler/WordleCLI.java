package com.augursolutions.wordler;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * A very simplistic command-line version of Wordle. Randomly chooses a word from one of the
 * previous Wordle solutions.
 * Dictionary as the solution. Sample play:
 * <pre>
 * This is WordleCLI
 * Randomly choosing a word ...
 * Puzzle is ready.
 * What is your guess?
 * GRAND
 * |   G    |   R    |   A    |   N    |   D    |
 * |  Gray  | Green  |  Gray  | Green  |  Gray  |
 * What is your guess?
 * ZZZZZ
 * Invalid guess, try again ...
 * What is your guess?
 * BRINK
 * |   B    |   R    |   I    |   N    |   K    |
 * |  Gray  | Green  |  Gray  | Green  | Green  |
 * What is your guess?
 * FRONT
 * |   F    |   R    |   O    |   N    |   T    |
 * |  Gray  | Green  |  Gray  | Green  |  Gray  |
 * What is your guess?
 * CRUNK
 * |   C    |   R    |   U    |   N    |   K    |
 * | Green  | Green  | Green  | Green  | Green  |
 * NAILED IT IN 4 GUESSES! (TOOK YOU LONG ENOUGH)
 * Bye
 * </pre>
 */
public class WordleCLI {
	
	public static void main(String args[]) {
		
		// Load full Wordle dictionary and also solutions only dictionary
		TreeMapLanguageDictionary wordleDictionary = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleDictionary,Path.of("./dictionaries","WordleDictionary.txt"));
		TreeMapLanguageDictionary wordleSolutions = new TreeMapLanguageDictionary();
		DictionaryLoadUtils.loadFromZyzzyva(wordleSolutions,Path.of("./dictionaries","WordleSolutions.txt"));
		WordleEngine wordleEngine = new WordleEngine(wordleDictionary);
		
		// Randomly choose a previous solution
		System.out.println("This is WordleCLI");
		System.out.println("Randomly choosing a word ... ");
		String solution = wordleSolutions.getRandomWord();
		wordleEngine.newPuzzle(solution);
		System.out.println("Puzzle is ready.");

		// While fewer than 6 guesses and puzzle unsolved, get input from System.in as a guess
		Scanner scan = new Scanner(System.in);
		while(!wordleEngine.isSolved() && wordleEngine.getGuessCount() < 6) {
			boolean newGuess = false;
			String guess="";
			while(!newGuess) {
				System.out.println("What is your guess?");
				guess = scan.nextLine();
				newGuess = wordleEngine.guessWord(guess);
				if(!newGuess) {
					System.out.println("Invalid guess, try again ...");
				}
			}
			System.out.println(displayGuess(guess));
			System.out.println(resposeArrayToString(wordleEngine.getResponse(wordleEngine.getGuessCount())));
		}

		if(wordleEngine.isSolved()) {
			System.out.println("NAILED IT IN " + wordleEngine.getGuessCount() + " GUESSES! (TOOK YOU LONG ENOUGH)");
		} else {
			System.out.println("WOMP WOMP ... SOLUTION WAS: " + solution);
		}

		scan.close();
		System.out.println("Bye.");
	}
	
	private static String displayGuess(String guess) {
		StringBuilder output = new StringBuilder("|");
		for(int i=0; i<guess.length(); i++) {
			output.append("   ").append(guess.charAt(i)).append("    |");
		}
		return output.toString();
	}
	
	private static String resposeArrayToString(WordleEngine.Response[] responses) {
		StringBuilder output = new StringBuilder("|");
		for(WordleEngine.Response r : responses) {
			switch(r) {
				case GRAY:
					output.append("  Gray  |");
					continue;
				case YELLOW:
					output.append(" Yellow |");
					continue;
				case GREEN:
					output.append(" Green  |");
					continue;
			}
		}
		return output.toString();
	}
}
