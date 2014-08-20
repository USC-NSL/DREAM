package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/28/13
 * Time: 10:22 PM
 */
public class MonitorWeight extends MonitorMetric {
    private final boolean ignoreExact;

    public MonitorWeight(boolean ignoreExact) {
        this.ignoreExact = ignoreExact;
    }

    @Override
    public Double compute(List<WildcardPattern> monitors) {
        double sum = 0;
        int count = 0;
        for (WildcardPattern monitor : monitors) {
            if (ignoreExact && monitor.getWildcardNum() == 0) {
                continue;
            }
            count++;
            sum += monitor.getWeight();
        }
        if (count == 0) {
            return null;
        }
        return sum / count;
    }

    @Override
    public String toString() {
        return "MonitorWeight" + (ignoreExact ? "_nonexact" : "");
    }
}
