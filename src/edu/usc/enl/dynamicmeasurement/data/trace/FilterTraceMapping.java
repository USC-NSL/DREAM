package edu.usc.enl.dynamicmeasurement.data.trace;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/22/13
 * Time: 8:40 PM<br/>
 * Keeps the mapping between traces and the prefix they will map to.
 */
public class FilterTraceMapping {
    private Map<WildcardPattern, InputTrace> traceInputMap;

    public FilterTraceMapping() {
        traceInputMap = new HashMap<>();
    }

    public InputTrace getInputTrace(WildcardPattern wildcardPattern) {
        for (Map.Entry<WildcardPattern, InputTrace> entry : traceInputMap.entrySet()) {
            if (entry.getKey().match(wildcardPattern)) {
                return entry.getValue();
            }
        }
        System.err.println("Trace input not found for " + wildcardPattern);
        System.exit(1);
        return null;
    }

    public void addTraffic(InputTrace inputTrace, int time) {
        traceInputMap.put(inputTrace.getWildcardPattern(), inputTrace);
    }
}
