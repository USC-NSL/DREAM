package edu.usc.enl.dynamicmeasurement.algorithms.matcher;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/24/13
 * Time: 10:32 AM  <br/>
 * Matches an exact item against a set of wildcardPatterns called monitors.
 * This is the linear matching implementation.
 */
public class Matcher {
    private Collection<WildcardPattern> monitors;

    public void setMonitors(Collection<WildcardPattern> monitors) {
        this.monitors = monitors;
    }

    /**
     * @param item
     * @return the wildcardpattern that matched the item. Note that it is the same object as passed in setMonitors.
     */
    public WildcardPattern match(long item) {
        for (WildcardPattern monitor : monitors) {
            if (monitor.match(item)) {
                return monitor;
            }
        }
        return null;
    }

    public WildcardPattern match(DataPacket pkt) {
        return match(pkt.getSrcIP());
    }
}