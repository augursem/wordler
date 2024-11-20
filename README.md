# Overview
`wordler` is a dictionary creation, filtering and analysis class for solving word puzzles.

This is still very much a work in progress. The main Dictionary class is fully functional and a class for solving the NYT Spelling Bee puzzle, SpellingBeeSolver serves as an initial proof of concept - 
solution is essentially just a filter applied to a dictionary with some formatted printing to the console.

The main goal is play with the idea of a best solution strategy for the NYT Wordle puzzle, which is essentially a word-based version of the classic game "Mastermind". Because this is a personal project
that I am working on because I enjoy working on it, detours (I suspect several) will be taken along the way. The current detour I find myself on is an investigation into how to best design a `Dictionary` class. 
For a discussion on this, see the [discussion about the myriad `Dictionary` classes](#why-are-there-so-many-dictionary-classes?)

# Documentation
Documentation (beyond this file) is located in the doc folder and is currently the javadoc generated as html.

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
Produces:
  PANGRAM(S):
  	PHANTOM
  
  WORDS OF LENGTH 9:
  	MANHATTAN
  WORDS OF LENGTH 8:
  	PHOTOMAP
  WORDS OF LENGTH 7:
  	MAHATMA
    ...
 
## Why Are There So Many Dictionary Classes?
My initial design had a single `Dictionary` class which is now called `TreeMapLanguageDictionary`. As I was developing this class it was clear to me that there were several other approaches I could have taken. 
Once I had it working, I stopped and asked myself the following questions:
1. What kind of performance improvement would I get from ignoring definitions and parts of speech, e.g. by getting rid of the `Word` objects?
2. What benefits (if any) does the M-ary Tree approach provide vs something simpler, e.g. just storing every word in a TreeSet?
3. How much memory do the different implementation options use?
4. How easy is it to implement filtering for these different classes? In which implementation is filtering words based on length and position of letters most efficient?
In order to answer these questions, I made the `Dictionary` class abstract and created six extensions of it. See the [Performance Tests](#performance-tests) section for answers to the above questions
5. What kind of performance gains do I get by abandoning alphabetical ordering (i.e. use a `HashMap`/`HashSet` instead of a `TreeMap`/`TreeSet`)?

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

# Long Words
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




