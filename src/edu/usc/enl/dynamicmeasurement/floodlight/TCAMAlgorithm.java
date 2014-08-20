package edu.usc.enl.dynamicmeasurement.floodlight;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/15/14
 * Time: 1:42 PM <br/>
 * This is a TCAM-based algorithm
 */
public interface TCAMAlgorithm {
    /**
     * return which switch this monitor must be saved. Although the client can find the answer itself but
     * for the sake of optimization, the algorithm may know that beforehand and help the client
     *
     * @param monitor
     * @return
     */
    public Collection<MonitorPoint> getWhichSwitch(WildcardPattern monitor);

    /**
     * @return prefixes to be monitored in the measurement phase
     */
    public Collection<WildcardPattern> getMonitors();
}
