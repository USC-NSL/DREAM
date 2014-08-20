package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/16/13
 * Time: 10:42 AM
 */
public class MonitorDepthCount extends MonitorMetric {
    private final int depth;

    public MonitorDepthCount(int depth) {
        this.depth = depth;
    }

    @Override
    public Double compute(List<WildcardPattern> monitors) {
        int count = 0;
        for (WildcardPattern monitor : monitors) {
            if (monitor.getWildcardNum() == WildcardPattern.TOTAL_LENGTH - depth) {
                count++;
            }
        }
        return 1.0 * count;
                // / monitors.size();
    }

    @Override
    public String toString() {
        return "depth_" + depth;
    }
}
