package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/27/13
 * Time: 9:16 PM
 */
public class MonitorsNum extends MonitorMetric{
    @Override
    public Double compute(List<WildcardPattern> monitors) {
        return 1.0*monitors.size();
    }

    @Override
    public String toString() {
        return "MonitorsNum";
    }
}
