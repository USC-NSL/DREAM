package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/29/13
 * Time: 9:27 AM
 */
public class HHHWeight extends HHHMetric {

    private final boolean ignoreExact;

    public HHHWeight(boolean ignoreExact) {
        this.ignoreExact = ignoreExact;
    }

    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        double sum = 0;
        int count = 0;
        for (WildcardPattern monitor : reportedHHH) {
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
        return "HHHWeight" + (ignoreExact ? "_nonexact" : "");
    }
}
