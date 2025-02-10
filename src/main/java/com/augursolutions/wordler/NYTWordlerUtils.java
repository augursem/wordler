package com.augursolutions.wordler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.logging.*;

/**
 * Utility class for interacting with the NYT Wordler API
 * @author Steven Major
 *
 */
public class NYTWordlerUtils {

	// First puzzle solution: https://www.nytimes.com/svc/wordle/v2/2021-06-19.json
	private static final String WORDLE_SERVICE_URL = "https://www.nytimes.com/svc/wordle/v2/";
	private static final String CHARSET = "UTF-8";
	
	private static final Logger LOGGER = Logger.getLogger( NYTWordlerUtils.class.getName() );
	
	public static void main(String args[]) {

		updatePreviousSolutionsDictionaryFile(Path.of("./dictionaries","WordleSolutions.txt"));
	}
	
	/**
	 * Creates a text file with every Wordle solution (including today's solution) and the date
	 * for which that word was the solution. e.g.
	 * <pre>
	 * humph 2021-06-22
     * sissy 2021-06-21
     * rebut 2021-06-20
     * cigar 2021-06-19
     * </pre>
	 * <p> 
	 * <b>CAUTION!:</b> If the file already exists, this method will replace it.
	 * @see {@link #getPuzzleDetails(Date)}
	 * @param dictionaryPath Path referencing the file to write to. 
	 */
	public static void createDictionaryFileFromPreviousSolutions(Path dictionaryPath) {
		if(dictionaryPath == null) {
			LOGGER.severe("dictionaryPath is null");
			return;
		}
		File dictionaryFile = null;
		boolean createdFile = false;
		// Try to initialize the file to write to - if it already exists, delete it and create a new one.
		try {
			dictionaryFile = dictionaryPath.toFile();
			if(!dictionaryFile.exists()) {
				createdFile = dictionaryFile.createNewFile();
			} else {
				createdFile = dictionaryFile.delete() && dictionaryFile.createNewFile();
			}
		} catch(Exception e) {
			createdFile = false;
		} finally {
			if(!createdFile || !dictionaryFile.canWrite()) {
				LOGGER.severe("Can't create, overwrite, or write to '" + dictionaryPath + "'");
				return;
			}
		}
		
		// Write out solutions to the specified file along with the dates
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(dictionaryFile), "utf-8"))) {
			// Use today's Wordle solution to get the total number of solutions 
			JSONObject todaysPuzzle = getPuzzleDetails(new Date());
			int daysSinceLaunch = Math.toIntExact((Long)todaysPuzzle.get("days_since_launch"));
			int numPuzzles = daysSinceLaunch + 1;
			
			//Starting with the first solution, get the solution from each day and write it to the file
			//Going forward in time this way (instead of starting with today and incrementing backwards)
			//allows us to update the file by appending future solutions to the end
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			//Adjust date back to day of first Wordle puzzle (
			calendar.add(Calendar.DAY_OF_MONTH, -1*daysSinceLaunch);
			for(int i=0; i<numPuzzles; i++) {
				JSONObject puzzle = getPuzzleDetails(calendar.getTime());
				String sln = (String)puzzle.get("solution");
				String slnDate = getDateEndpointString(calendar.getTime());
				if(i > 0)
					writer.write("\n");
				writer.write(sln + " " + slnDate);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the dictionary file with all Wordle solutions by determining the last Date that
	 * a solution is recorded (last line of the file) and then adding solutions for all subsequent
	 * days up to and including today.
	 * @see {@link #createDictionaryFileFromPreviousSolutions(dictionaryPath)}
	 * @param dictionaryPath Path referencing the file to write to. 
	 * @return true if it runs without error
	 */
	public static boolean updatePreviousSolutionsDictionaryFile(Path dictionaryPath) {
		if(dictionaryPath == null) {
			LOGGER.severe("dictionaryPath is null");
			return false;
		}
		File dictionaryFile = null;
		boolean canReadWriteToFile = false;
		// Make sure we can read and write from the file. 
		try {
			dictionaryFile = dictionaryPath.toFile();
			if(!dictionaryFile.exists()) {
				canReadWriteToFile = dictionaryFile.createNewFile() && dictionaryFile.canRead() && dictionaryFile.canWrite();
			} else {
				canReadWriteToFile = dictionaryFile.canRead() && dictionaryFile.canWrite();
			}
		} catch(Exception e) {
			canReadWriteToFile = false;
			e.printStackTrace();
		} finally {
			if(!canReadWriteToFile) {
				LOGGER.severe("Can't read and update '" + dictionaryPath + "' - check that it exists and that it is readable wnd writable.");
				return false;
			}
		}
		
		// Read the existing file to get the last date for which there is a solution - should be the last line of the file
		String lastRecordedDateString = null;
        try (BufferedReader br = new BufferedReader(new FileReader(dictionaryFile))) {
            String line;
            int i = 0;
    		// 
            while ((line = br.readLine()) != null) {
            	i++;
            	String[] words = line.split("\s+");
            	if(words == null || words.length != 2) {
            		LOGGER.warning("At line " + i + " of '" + dictionaryPath + "': expected '<word> <date>' but found '" + line + "'");
            	} else {
            		// YYYY-MM-DD
            		lastRecordedDateString = words[1];
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("something went wrong when trying to read '" + dictionaryPath + "'");
			return false;
        }
        if(lastRecordedDateString == null || lastRecordedDateString.isEmpty()) {
        	LOGGER.severe("Something went wrong when reading '" + dictionaryPath + "' to retrieve the last recorded solution date.");
        	return false;
        }
        
        // Go from the day after lastRecordedDate to today and get Wordle solutions
		// Write out solutions to the specified file along with the dates
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	         new FileOutputStream(dictionaryFile, true), "utf-8"))) {
			
			//Starting with tiday's date, get the solution from each day and store in a HashSet
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
	        Date lastRecordedDate = formatter.parse(lastRecordedDateString);
			
			// Sanity check - make sure lastRecordedDate is before today
	        Date today = new Date();
			if(!lastRecordedDate.before(today)) {
				LOGGER.severe("Last date with a wordle solution in '" + dictionaryPath + "' is: " + lastRecordedDateString + ", which is in the future.");
				return false;
			}
			
			// Add each day since the last recorded solution date to the dictionary
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(lastRecordedDate);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			// NOTE: 'today' is timeStamped at some time after 0 hrs and 0 seconds, so even when
			// calendar.getTime() and 'today' refer to the same day, calendar.getTime().before(today)
			// will be true, since calendar is set using YYYY-MM-DD and thus has 0 for hours and seconds
			while(calendar.getTime().before(today)) {
				//Move forward one day
				// Get the solution and add it to the file
				JSONObject puzzle = getPuzzleDetails(calendar.getTime());
				String sln = (String)puzzle.get("solution");
				String slnDate = getDateEndpointString(calendar.getTime());
				writer.write("\n");
				writer.write(sln + " " + slnDate);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
        
		return true;
	}
	
	/**
	 * Creates a {@link LinkedHashSet} with every Wordle solution (including today's solution).
	 * See {@link #getPuzzleDetails(Date)}
	 * @return A {@link LinkedHashSet} with all previous solutions - first item in the list is 
	 * today's solution 
	 */
	public static LinkedHashSet<String> getAllPreviousSolutions() {
		LinkedHashSet<String> solutions = new LinkedHashSet<>();
		try {
			// Use today's Wordle solution to get the total number of solutions 
			JSONObject todaysPuzzle = getPuzzleDetails(new Date());
			Long puzzleNumber = (Long)todaysPuzzle.get("days_since_launch");
			
			//Starting with tiday's date, get the solution from each day and store in a HashSet
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			for(int i=0; i<=puzzleNumber; i++) {
				JSONObject puzzle = getPuzzleDetails(calendar.getTime());
				String sln = (String)puzzle.get("solution");
				//Check for duplicate solutions, just as a point of curiosity
				String slnDate = getDateEndpointString(calendar.getTime());
				if(solutions.contains(sln)) {
					LOGGER.info("Duplicate solution found at date | solution '" + slnDate + "' | " + sln + "'");
				} else {
					solutions.add(sln);
				}
				calendar.add(Calendar.DAY_OF_MONTH, -1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return solutions;	
	}
	
	private static String getDateEndpointString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}
	
	/**
	 * Gets the Wordle solution for the specified date by making a request to
	 * www.nytimes.com/svc/wordle/v2/<YYYY-MM-DD>.json.
	 * Note that this does not verify that the date is valid. 
	 * For example, if the Date value is June 20, 2021, this method 
	 * makes a request to:
	 * <br>
	 * <b>https://www.nytimes.com/svc/wordle/v2/2021-06-20.json</b>
	 * <br>
	 * and returns the JSON object:
	 * <b>
	 * <pre>
	 * {
	 *   "id":2,
	 *   "solution":"rebut",
	 *   "print_date":"2021-06-20",
	 *   "days_since_launch":1
	 * }
	 * </pre>
	 * </b>
	 * @param date A {@link Date} to get the Wordle solution for
	 * @return JSON object with id (first puzzle has id 1), solution, print_date, and days_since_launch.
	 */
	private static JSONObject getPuzzleDetails(Date date) {
		try {
			String url = WORDLE_SERVICE_URL + getDateEndpointString(date) + ".json";
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("Accept-Charset", CHARSET);
			InputStream response = connection.getInputStream();
			JSONParser jsonParser = new JSONParser();
			return (JSONObject)jsonParser.parse(
				      new InputStreamReader(response, "UTF-8"));
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
