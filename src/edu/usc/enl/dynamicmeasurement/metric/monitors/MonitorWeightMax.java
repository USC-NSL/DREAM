package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/28/13
 * Time: 10:22 PM
 */
public class MonitorWeightMax extends MonitorMetric {
    private final boolean ignoreExact;

    public MonitorWeightMax(boolean ignoreExact) {
        this.ignoreExact = ignoreExact;
    }

    @Override
    public Double compute(List<WildcardPattern> monitors) {
        double max = -1;
        for (WildcardPattern monitor : monitors) {
            if (ignoreExact && monitor.getWildcardNum() == 0) {
                continue;
            }
            if (max<monitor.getWeight()){
                max=monitor.getWeight();
            }
        }
        if (max < 0) {
            return null;
        }
        return max;
    }

    @Override
    public String toString() {
        return "MonitorWeight_Max" + (ignoreExact ? "_nonexact" : "");
    }
}
