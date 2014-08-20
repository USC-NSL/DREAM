package edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/25/13
 * Time: 9:36 AM <br/>
 * The implementation of this interface can be used in an algorithm to initialize with a set of prefixes
 * that cover all leaves of the prefix tree
 */
public interface NeedInitHHHAlgorithm {
    /**
     * Add a prefix to the set of monitors
     *
     * @param wildcardPattern
     */
    public void addMonitor(WildcardPattern wildcardPattern);

    /**
     * Polls a prefix. This can be any of the prefixes in the set.
     * Different patterns of trees can be created by returning first or last or deepest of the current prefixes
     *
     * @return
     */
    public WildcardPattern pollAMonitor();
}
