# Overview
`wordler` is a dictionary creation, filtering and analysis class for solving word puzzles. 

This is still very much a work in progress. The main Dictionary class is fully functional and a class for solving the NYT Spelling Bee puzzle, [SpellingBeeSolver](#SpellingBeeSolver) serves as an initial proof of concept - 
solution is essentially just a filter applied to a dictionary with some formatted printing to the console. The [WordleEngine](#WordleEngine), [WordleStrategy](#WordleStrategy), and [WordleTournament](#WordleTournament) classes
are meant to be used together to develop and compare different solution strategies for the NYT Wordle puzzle. The [WordleCLI](#WordleCLI) class uses [WordleEngine](#WordleEngine) to create a (very) simple command-line 
version of Wordle.

The main goal is play with the idea of a best solution strategy for the NYT Wordle puzzle, which is essentially a word-based version of the classic game "Mastermind". Because this is a personal project
that I am working on because I enjoy working on it, detours (I suspect several) will be taken along the way. For example, I spent a bit of time thinking about how to best design a `Dictionary` class. 
For a discussion on this, see the [discussion about the myriad `Dictionary` classes](#why-are-there-so-many-dictionary-classes?)

# Documentation
Documentation (beyond this file) is located in the doc folder and is currently the javadoc generated as html.

## WordleEngine
Provides basic functionality for creating a Wordle puzzle. It is initialized with a dictionary of valid guesses and provides functionality to create a puzzle by specifying a solution, and then provides the standard 
Wordle response to guesses made against that puzzle (green indicates the letter of the guessed word is in the solution and in the exact position guessed, yellow indicates that the letter of the guessed word
is in the solution but in a different position guessed, and gray indicates that the guessed letter is not in the solution (or, if it appears more than once in the guessed word, that the number of 
instances of that letter in the solution is less than the number of instances in the guessed word).

The WordleEngine class is used to run the cammand-line Wordle game provided by [WordleCLI](#WordleCLI) and also the solution strategy comparison class, [WordleTournament](#WordleTournament).

## WordleCLI
Provides a very simple command-line version of Wordle. The class should be invoked with no arguments - a puzzle solution will be chosen at random. 

### Sample Usage
```cmd
This is WordleCLI
Randomly choosing a word ...
Puzzle is ready.
What is your guess?
GRAND
|   G    |   R    |   A    |   N    |   D    |
|  Gray  | Green  |  Gray  | Green  |  Gray  |
What is your guess?
ZZZZZ
Invalid guess, try again ...
What is your guess?
BRINK
|   B    |   R    |   I    |   N    |   K    |
|  Gray  | Green  |  Gray  | Green  | Green  |
What is your guess?
FRONT
|   F    |   R    |   O    |   N    |   T    |
|  Gray  | Green  |  Gray  | Green  |  Gray  |
What is your guess?
CRUNK
|   C    |   R    |   U    |   N    |   K    |
| Green  | Green  | Green  | Green  | Green  |
NAILED IT IN 4 GUESSES! (TOOK YOU LONG ENOUGH)
Bye
```

## WordleStrategy
This is an interface that describes a very simple behavior for a Wordle solution class - it should produce a first guess and also, given a previous guess and previous response,
produce a next guess. The [SimpleWordleStrategy](#SimpleWordleStrategy) class is currently the only implementation of it

### SimpleWordleStrategy
This is a proof-of-concept class for other solution strategies. It keeps a full dictionary of words to choose from which is trimmed down with each guess. The first guess is always "GRAND" 
(which in my experience does pretty ok). To get a next guess, the class takes the response and removes all words from its dictionary that don't meet the information provided by the response.
The first word (alphabetically) in the reduced dictionary is used as the next guess.

Details are in the class itself, but the logic is a two stage process:
 1. Sort letters into 4 categories: Green, Yellow, Gray, Grayish
 2. Check word against letters in each category, if at any point there is a discrepancy, remove the word from the dictionary (and stop checking remaining categories).
 
#### Green Letters
Straightforward - all letters that were green in the response. If a word in the dictionary does not have that letter in that position, it is removed

#### Yellow Letters
Also straightforward - all letters that were yellow in the response. A word is removed if it either doesn't contain the yellow letter or if it has that letter in the same position.

#### Gray Letters
Letters that gray in the response are gray and not grayish if the same letter does not also appear in the guess and get marked as green or yellow, meaning this letter just doesn't
appear in the word at all. Any word that contains this letter is removed.

#### Grayish Letters
Grayish letters are letters that were gray, but where the same letter was marked as green or yellow in a different location. For example, if the solution is "GREEN" and the guess is "GEESE",
the last E in GEESE is marked as GRAY. Removing all words with an E would be a mistake though. In this case, the 3rd E in GEESE being gray tells us that there are exactly 2 Es in the solution.
For grayish letters, there are 2 rounds of filtering performed:
 1. Remove all words that contain the letter in the exact position in which it was guessed.
 2. Count the number of times that letter appears in the Green and Yellow lists - this is how many times the letter appears in the word. Remove any words that don't contain exactly that many
 instances of the letter

## WordleTournament
This class is meant to take some number of [WordleStrategy](#WordleStrategy) objects and to have them all try to solve the same collection of puzzles. The distribution of results for each strategy is computed
(number of puzzles solved in 1 guess, 2 guesses, .... , 6 guesses, and number of puzzles failed). 

### SimpleWordleStrategy Results
Using all previous wordle solutions (1,332 when it was run), the [SimpleWordleStrategy](#SimpleWordleStrategy) had a 98.9% succes rate:
| Number of Guesses   | Frequency    |
| ------------------- | ------------ |
| 1                   | 1 (0.08%)    |
| 2                   | 23 (1.73%)   |
| 3                   | 135 (10.14%) |
| 4                   | 400 (30.03%) |
| 5                   | 379 (28.45%) |
| 6                   | 246 (18.47%) |
| 7+ (fail)           | 148 (11.11%) |

## SpellingBeeSolver
The [New York Times Spelling Bee](https://www.nytimes.com/puzzles/spelling-bee) is a daily online puzzle that consists of 7 letters. The goal is to find all words that use only those letters, with the caveat that one of 
the seven letters must be included in every word. The `SpellingBeeSolver` class finds all solutions to the puzzle for a given dictionary. A word that uses every letter in the puzzle is called a *pangram*.

### Sample usage
```java
	SpellingBeeSolver slvr = new SpellingBeeSolver();
	slvr.setDictionary(dictionary);
	slvr.setPossibleLetters("PHANTO");
	slvr.setRequiredLetter("M");
	SpellingBeeSolution sln = slvr.solve();
	sln.prettyPrint();
```
produce the output:
```
  PANGRAM(S):
  	PHANTOM
  
  WORDS OF LENGTH 9:
  	MANHATTAN
  WORDS OF LENGTH 8:
  	PHOTOMAP
  WORDS OF LENGTH 7:
  	MAHATMA
    ...
```

## Why Are There So Many Dictionary Classes?
My initial design had a single `Dictionary` class which is now called `TreeMapLanguageDictionary`. As I was developing this class it was clear to me that there were several other approaches I could have taken. 
Once I had it working, I stopped and asked myself the following questions:
1. What kind of performance improvement would I get from ignoring definitions and parts of speech, e.g. by getting rid of the `Word` objects?
2. What benefits (if any) does the M-ary Tree approach provide vs something simpler, e.g. just storing every word in a TreeSet?
3. How much memory do the different implementation options use?
4. How easy is it to implement filtering for these different classes? In which implementation is filtering words based on length and position of letters most efficient?
5. What kind of performance gains do I get by abandoning alphabetical ordering (i.e. use a `HashMap`/`HashSet` instead of a `TreeMap`/`TreeSet`)?

In order to answer these questions, I made the `Dictionary` class abstract and created six extensions of it. See the [Performance Tests](#performance-tests) section for answers to the above questions

### Dictionary (Abstract Class)
All `Dictionary` classes have an `add`, `remove`, `contains`, and `getWord` method and implement `Serializable` as well as `Iterable<Word>`. Further, to support future designs for testing puzzle solution
strategies, random word generation is supported with the `setSeed()`, `getRandomWord()`, and `getRandomWordLookupTable()` methods.

### TreeSet / HashSet Dictionaries
These are the simplest implementations - all words are stored in a `HashSet` or `TreeSet` and adding / retrieving words uses the native methods for those objects. Words are stored as Strings.

### HashSetLanguageDictionary
Similar to `HashSetDictionary`, except words are stored as `Word` objects

### HashMapDictionary / TreeMapDictionary
Simplified version of the original design; each `Dictionary` is an M-ary tree where each node represents a single letter in a word. In this way, the letter "B" in "BAT", "BOY", and "BIRD" is one
node in the tree; in fact every word that starts with "B" will share the same node for that letter. Similarly, the letter "O" in "BOY" and "BOX" is a single node in the tree, and so on. 
The characters corresponding to the ASCII values 02 and 03 are reserved for the root node of the tree and for word ending nodes respectively. A word ending node indicates that the sequence of letters leading up to that node
represent a word. 

As an example, consider a Dictionary object with the words "ASK", "BE", "BED", and "BEST". The associated tree
would look like the image below, with (R) indicating the root node and (E) indicating nodes that mark the end of a word.
```
       A-S-K-(E)
      /
   (R) 
      \
       B   (E)
        \ / 
         E-D-(E)  
          \
           S-T-(E)
```
***Note: that 'AS' is not a word in this dictionary since the 'S' in 'ASK' has no word ending child node***

### TreeSetLanguageDictionary
Similar to the `TreeMapDictionary` class, with the addition of having each word ending node linked to a Word object so that definitions and parts of speech can be stored.

## Filtering
Filtering is currently only implemented for `Dictionary` classes that extend the `HashMapDictionary` class, namely:
 - `HashMapDictionary`
 - `TreeMapDictionary`
 - `TreeMapLanguageDictionary`

It is my intention to make filtering work across all classes and to include filtering in perforance testing.

## Random Word generation
There are two ways to generate random words:
1. By calling `getRandomWord()` 
2. By first generating a lookup table of words with `getRandomWordLookupTable()` and then passing this to `getRandomWord(Word[])`, e.g.
```java
Dictionary d = new Dictionary();
d.add("abc");
d.add("def");
d.add("ghi");
int nDraws = 10;
Word[] allWords = d.getRandomWordLookupTable();
for(int i=0; i<nDraws; i++) {
	System.out.println(d.getRandomWord(allWords));
}
```
To get reproduceable results, set the random seed for random word generation with the `Dictionary.setSeed(int seed)` method.

# Performance Tests
These are ongoing, but initial findings are that (unsurprisingly) the simplest solutions are the best ones. Run times for loading, checlking for the existence of a word, and for serializtion were compared for each of the 
`Dictionary` classes. The size of the `Dictionary` objects after serialization was also compared. A proper test of memory utilization would require looking at memory allocated to different
classes in the heap when the different `Dictionary` objects are loaded. Using the `MemoryMXBean` class to investigate heap usage before and after running some code provides unreliable results,
as it is not possible to force garbage collection. Jerónimo López developed a [great solution for this](https://medium.com/@jerolba/measuring-actual-memory-consumption-in-java-jmnemohistosyne-5eed2c1edd65),
which I plan to utilize shortly. 

All tests were run on my laptop; Intel(R) Core(TM) i7-10750H CPU with 32 GB of RAM


## Load Times
Across classes, loading the entire Scrabble Dictionary (along with definitions for `Dictionary` classes that supported this) was fairly fast. For each class,
the dictionary was loaded from a text file with approximately 200,000 words 100 times.

| Dictionary Class          | Time To Perform 100 Loads |
| ------------------------- | ------------------------- |
| HashSetDictionary         |  4.3060780 s              |
| HashSetLanguageDictionary |  4.6314748 s              |
| TreeSetDictionary         |  6.6584390 s              |
| HashMapDictionary         |  7.9668297 s              |
| TreeMapDictionary         | 10.2557563 s              |
| TreeMapLanguageDictionary | 29.1853386 s              |

## Word Lookup Times
Timing for checking the presence of a word (calling `contains`) was tested for short (2-4 letters), medium (5-8 letters) and long 
(9+ letters, including an 18 letter word). Each list of words was iterated through 10,000,000 times, meaning if a list contained 5 words, 
`contains()` was called on each word 2,000,000 times.

**SUMMARY**: The `HashSetDictionary` is universally fastest. For smaller words, the `TreeMapDictionary` outperforms `TreeSetDictionary`, which means that 
for an option that returns results alphabetically, this is the fastes look-up option for small words. The M-ary tree design performs worse as words get longer 
relative to the `HashSet`/`TreeSet` implementations. 

### Short Words 
| Dictionary Class          | Time To Lookup  10,000,000 Words |
| ------------------------- | -------------------------------- |
| HashSetDictionary         |  0.0960700 s                     |
| HashMapDictionary         |  0.2836958 s                     |
| TreeMapDictionary         |  0.4689360 s                     |
| HashSetLanguageDictionary |  0.5635473 s                     |
| TreeSetDictionary         |  0.6648884 s                     |
| TreeMapLanguageDictionary |  0.5718636 s                     |

### Medium Words
| Dictionary Class          | Time To Lookup  10,000,000 Words |
| ------------------------- | -------------------------------- |
| HashSetDictionary         |  0.1215837 s                     |
| HashMapDictionary         |  0.6315770 s                     |
| HashSetLanguageDictionary |  0.6490033 s                     |
| TreeSetDictionary         |  0.8530115 s                     |
| TreeMapDictionary         |  0.9454727 s                     |
| TreeMapLanguageDictionary |  1.0859602 s                     |

## Long Words
| Dictionary Class          | Time To Lookup  10,000,000 Words |
| ------------------------- | -------------------------------- |
| HashSetDictionary         |  0.1212057 s                     |
| HashSetLanguageDictionary |  0.7236824 s                     |
| TreeSetDictionary         |  0.9010327 s                     |
| HashMapDictionary         |  1.6106797 s                     |
| TreeMapDictionary         |  1.8036687 s                     |
| TreeMapLanguageDictionary |  2.0115300 s                     |

## Serialization 
Time to serialize and alo size after serialization for each class is listed below:


| Dictionary Class          | Time To Serialize Out 196,601 words | Size of Serialzed Object |
| ------------------------- | ----------------------------------- | ------------------------ |
| HashSetDictionary         |  0.6604575 s                        | 1 KB                     |
| TreeSetDictionary         |  0.6062664 s                        | 1 KB                     |
| HashSetLanguageDictionary |  5.8258055 s                        | 2 KB                     |
| HashMapDictionary         | 14.6392105 s                        | 4 KB                     |
| TreeMapDictionary         | 14.9947681 s                        | 4 KB                     |
| TreeMapLanguageDictionary | 21.0286168 s                        | 5 KB                     |




