package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/29/13
 * Time: 9:26 AM
 */
public class MaxHHH extends HHHMetric {
    private final boolean ignoreExact;

    public MaxHHH(boolean ignoreExact) {
        this.ignoreExact = ignoreExact;
    }

    @Override
    public String toString() {
        return "HHHWeight_Max" + (ignoreExact ? "_nonexact" : "");
    }

    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        double max = -1;
        for (WildcardPattern monitor : reportedHHH) {
            if (ignoreExact && monitor.getWildcardNum() == 0) {
                continue;
            }
            if (max < monitor.getWeight()) {
                max = monitor.getWeight();
            }
        }
        if (max < 0) {
            return null;
        }
        return max;
    }
}
