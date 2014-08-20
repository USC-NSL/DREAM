package edu.usc.enl.dynamicmeasurement.util.profile;

import edu.usc.enl.dynamicmeasurement.util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/4/13
 * Time: 4:26 PM  <br/>
 * This class helps to track the latency between two actions in nanoseconds.
 */
public class LatencyProfiler {
    private Map<String, Long> profile;
    private String lastKey;
    private PrintWriter profileWriter;
    private int writeEpoch;

    public LatencyProfiler(Class c) {
        this(Util.getRootFolder() + "/profile_" + c.getSimpleName() + ".csv");
    }

    public LatencyProfiler(String outputFile) {
        profile = new HashMap<>();
        writeEpoch = 0;
        try {
            profileWriter = new PrintWriter(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start tracking the timer for key "key". Can be called once for each key.
     * Running that again will reset the timestamp of the start of the key.
     *
     * @param key
     */
    public void start(String key) {
        profile.put(key, System.nanoTime());
        lastKey = key;
    }

    /**
     * stop tracking the timer for key "key". Can be called once after starting te key
     *
     * @param key
     */
    public void stop(String key) {
        if (profile.containsKey(key)) {
            profile.put(key, System.nanoTime() - profile.get(key));
        }
        lastKey = null;
    }

    /**
     * Stops tracking for the latest key and starts for the new one
     *
     * @param key
     */
    public void sequentialRecord(String key) {
        if (lastKey != null) {
            if (key != null && !lastKey.equals(key)) {
                stop(lastKey);//makes lastkey null
                start(key);
            } else {
                stop(lastKey);
            }
        } else if (key != null) {
            start(key);
        }
    }

    /**
     * decides to do starting or stopping. Can be called twice for a key.
     *
     * @param key
     */
    public void record(String key) {
        Long timeStamp = profile.get(key);
        if (timeStamp == null) {
            profile.put(key, System.nanoTime());
            lastKey = key;
        } else {
            profile.put(key, System.nanoTime() - timeStamp);
            lastKey = null;
        }
    }

    /**
     * clear all statistics
     */
    public void clear() {
        profile.clear();
        lastKey = null;
    }

    /**
     * write the statistics in the file and clear
     */
    public void write() {
        if (profileWriter != null) {
            for (Map.Entry<String, Long> entry : profile.entrySet()) {
                profileWriter.println(writeEpoch + "," + entry.getKey() + "," + entry.getValue());
            }
            profileWriter.flush();
        }
        writeEpoch++;
        clear();
    }

    public void finish() {
        if (profileWriter != null) {
            profileWriter.close();
        }
    }

    public Long get(String key) {
        return profile.get(key);
    }

}
