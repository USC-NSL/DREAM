package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/27/13
 * Time: 9:16 PM
 */
public class MonitorsDepth extends MonitorMetric {
    @Override
    public Double compute(List<WildcardPattern> monitors) {
        double depth = 0;
        for (WildcardPattern monitor : monitors) {
            depth += WildcardPattern.TOTAL_LENGTH - monitor.getWildcardNum();
        }
        return depth / monitors.size();
    }

    @Override
    public String toString() {
        return "MonitorsDepth";
    }
}
