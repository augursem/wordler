# Overview
`wordler` is a dictionary creation, filtering and analysis class for solving word puzzles.

This is still very much a work in progress. The main Dictionary class is fully functional and a class for solving the NYT Spelling Bee puzzle, SpellingBeeSolver serves as an initial proof of concept - 
solution is essentially just a filter applied to a dictionary with some formatted printing to the console.

The main goal is play with the idea of a best solution strategy for the NYT Wordle puzzle, which is essentially a word-based version of the classic game "Mastermind". Because this is a personal project
that I am working on because I enjoy working on it, detours (I suspect several) will be taken along the way. The current detour I find myself on is an investigation into how to best design a `Dictionary` class. 
For a discussion on this, see the [discussion about the myriad `Dictionary` classes][#why-are-there-so-many-dictionary-classes?]

# Documentation
Documentation (beyond this file) is located in the doc folder and is currently the javadoc generated as html.

## Why Are There So Many Dictionary Classes?
My initial design had a single `Dictionary` class which is now called `LanguageDictionary`. As I was developing this class it was clear to me that were were several other approaches I could have taken. 
Once I had it workging, I stopped and asked myself the following questions:
1. What kind of performance improvement would I get from ignoring definitions and parts of speech, e.g. by getting rid of the `Word` objects?
2. What benefits (if any) does this approach provide vs something simpler, e.g. just storing every word in a TreeSet?
3. How much memory do the different implementation options use?
4. How easy is it to implement filtering for these different classes? In which implementation is filtering words based on length and position of letters most efficient?
In order to answer these questions, I made the `Dictionary` class abstract and created five extensions of it. See the [Performance Tests][#performance-tests] section for answers to the above questions

## Dictionary (Abstract Class)

# Performance Tests
## Performance Improvement from Dropping the `Word` Objects From `LanguageDictionary` Class




