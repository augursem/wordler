package com.augursolutions.wordlerTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;

import org.reflections.Reflections;

import com.augursolutions.wordler.*;

/**
 * Collection of tests that compare speed and memory utilization of different {@link Dictionary} classes
 * @author Steven Major
 *
 */
public class DictionaryPerformaceTests {

	private static Set<Class<? extends Dictionary>> dictionaryClasses = null;
	
	static {
		// Collect all extensions of the Dictionary class
		Reflections reflections = new Reflections("com.augursolutions");    
		dictionaryClasses = reflections.getSubTypesOf(Dictionary.class);
	}
	public static void main(String[] args) {
		boolean fastRun = (args.length > 0 && args[0].toUpperCase().equals("FAST"));	
		
		String runMode = fastRun ? "FAST" : "STANDARD (aka SLOW)";
		System.out.println("Running Tests In " + runMode + " Mode ...");
		DictionaryPerformaceTests test = new DictionaryPerformaceTests();
		int nLoads         = fastRun ? 2 : 100;
		int nContainsCalls = fastRun ? 10000 : 10000000;
		test.testLoadTimes(nLoads);
		test.testContainsCallTimes(nContainsCalls);
		test.testSerialization(fastRun);
	}

	/**
	 * Load each of the dictionaries in from a text file containing words and definitions {@code NLoads} times and
	 * measure how long each sequence of loads takes.
	 * @param NLoads Number of times to load each of the dictionary types
	 */
	private void testLoadTimes(int nLoads) {
		Timer timer = new Timer();
		timer.mark("Start");
		for(Class<? extends Dictionary> klass :dictionaryClasses) {
			for(int i=0; i<nLoads; i++) {
				try {
					Dictionary d = klass.getConstructor().newInstance();
					DictionaryLoadUtils.loadFromZyzzyva(d,Path.of("./test/dictionaries","NWL2023.txt"));
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			timer.mark("Loaded '" + klass.getSimpleName() + "' from file " + nLoads + " times.");
		}
		timer.printSplits();
	}

	private void testContainsCallTimes(int nCalls) {
		String[] shortWords = { "AT", "BOAT", "CAT", "ZEN", "YES", "HELP", "DOG", "MOW", "WOW", "JUST"
		};

		String[] mediumWords = { "ZEBRA", "BANANA", "PNEUMONIA", "CARROT", "SEVERAL", "LENGTHY", "MEDIUM",
		                         "TROPICAL", "HAZARD", "BUZZARD", "UNDERTOW"
		};

		String[] longWords = { "XYLOPHONE", "STEGOSAURUS", "REFRIGERATOR", "ANTIDISESTABLISHMENTARIANISM",
		                       "COUNTERREVOLUTIONARY", "INSTITUTIONIALIZATION", "INCOMPREHENSIBLE", "OTORHINOLARYNGOLOGICAL"
		};

		Timer timer = new Timer();
		timer.mark("Start");
		for(Class<? extends Dictionary> klass :dictionaryClasses) {
			try {
				Dictionary d = klass.getConstructor().newInstance();
				DictionaryLoadUtils.loadFromZyzzyva(d,Path.of("./test/dictionaries","NWL2023PlusLongWords.txt"));
				timer.mark("Loaded '" + klass.getSimpleName());
				for(int i=0; i<nCalls; i++) {
					String w = shortWords[i%shortWords.length];
					d.contains(w);
				}
				timer.mark("Called 'contains' using SHORT words against '" + klass.getSimpleName() + "'  " + nCalls + " times.");
				for(int i=0; i<nCalls; i++) {
					String w = mediumWords[i%mediumWords.length];
					d.contains(w);
				}
				timer.mark("Called 'contains' using MEDIUM words against '" + klass.getSimpleName() + "'  " + nCalls + " times.");
				for(int i=0; i<nCalls; i++) {
					String w = longWords[i%longWords.length];
					d.contains(w);
				}
				timer.mark("Called 'contains' using LONG words against '" + klass.getSimpleName() + "'  " + nCalls + " times.");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		timer.printSplits();
	}

	private void testSerialization(boolean fastRun) {
		try {
			Timer timer = new Timer();
			timer.mark("Start");
			File tmpDir = Path.of("test","tmp").toFile();

			// If tmp already exists, delete its contents
			if(tmpDir.exists()) {
				String[] entries = tmpDir.list();
				for (String s : entries) {
				    File currentFile = new File(tmpDir.getPath(), s);
				    currentFile.delete();
				}
			}

			// Create tmp if it doesn't exist and make sure we can write to it
			if(!tmpDir.exists()) {
				tmpDir.mkdirs();
			}
			if(!tmpDir.exists() || !tmpDir.canWrite()) {
				System.out.println("ERROR: can't create / write to test/tmp");
				return;
			}
			timer.mark("tmp directory cleared and ready");

			// For each Dictionary class, load in the NWL2023 dictionary and serialize out.
			Map<String,Dictionary> dMap = new HashMap<>();
			for(Class<? extends Dictionary> klass :dictionaryClasses) {
				Dictionary d = klass.getConstructor().newInstance();
				if(fastRun) {
					DictionaryLoadUtils.loadFromZyzzyva(d,Path.of("./test/dictionaries","small_no_definitions.txt"));
				} else {
					DictionaryLoadUtils.loadFromZyzzyva(d,Path.of("./test/dictionaries","NWL2023.txt"));
				}
				dMap.put(klass.getSimpleName(), (Dictionary)d);
			}
			timer.mark("All Dictionary objects read into memory");

			for(Class<? extends Dictionary> klass :dictionaryClasses) {
				Dictionary d = dMap.get(klass.getSimpleName());
				File dictionaryOut = Path.of(tmpDir.getAbsolutePath(),klass.getSimpleName()+".dctnry").toFile();
				FileOutputStream fout = new FileOutputStream(dictionaryOut);
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(d);
				oos.close();
				fout.close();
				timer.mark("Serialized out Dictionary class '" + klass.getSimpleName() + "'");
			}
			timer.printSplits();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public static class Timer {
		private LinkedList<Pair<Instant,String>> splits;

		public Timer() {
			this.reset();
		}

		public void reset() {
			splits = new LinkedList<>();
		}

		public void mark(String s) {
			splits.add(new Pair<>(Instant.now(),s));
		}

		public void printSplits() {
			if(splits == null || splits.isEmpty()) {
				System.out.println("NO TIMING RECORDS");
				return;
			}
			Instant prevInstant = null;
			Instant instant;
			for (Pair<Instant, String> split : splits) {
				String description = split.value();
				instant = split.key();
				LocalTime time = instant.atZone(ZoneOffset.UTC).toLocalTime();
				System.out.println(description);
				System.out.println("    AT: " + time);
				if(prevInstant != null) {
				  Duration elapsed = Duration.between(prevInstant, instant);
				  long seconds = elapsed.getSeconds();
				  long nanoSeconds = elapsed.getNano();
					System.out.println("    DURATION: " + seconds + "s, " + nanoSeconds + "ns  (" + (seconds + nanoSeconds/1000000000.0) + " s)");
				}
				prevInstant = instant;
			}

		}
	}

	public static void memoryUsage() {
	    String name = ManagementFactory.getRuntimeMXBean().getName();
	    String PID = name.substring(0, name.indexOf("@"));
	    Process p = null;
	    try {
	    	p = Runtime.getRuntime().exec("jcmd " + PID + " GC.class_histogram");
	    } catch(Exception e) {

	    }
	    try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
	        input.lines().forEach(System.out::println);
	    } catch (Exception e) {

	    }
	}

	/**
	 * NOT USED - RATHER THAN MAKE THIS FUNCTIONAL, SHOULD IMPOTT EXISTING SOLUTION FROM:
	 * https://medium.com/@jerolba/measuring-actual-memory-consumption-in-java-jmnemohistosyne-5eed2c1edd65
	 */
	public static class MemoryTracker {
		MemoryMXBean mbean;
		private LinkedList<Pair<MemoryUsage,String>> splits;

		public MemoryTracker() {
			mbean = ManagementFactory.getMemoryMXBean();
			this.reset();
		}

		public void reset() {
			splits = new LinkedList<>();
		}

		public void mark(String s) {
			splits.add(new Pair<>(mbean.getHeapMemoryUsage(),s));
		}

		public void printMemoryUsage() {

		}

	}

	private record Pair<K, V>(K key, V value) {
	    // intentionally empty
	}
}
