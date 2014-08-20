package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/28/13
 * Time: 6:17 AM
 */
public class MaxHHHRatio extends HHHMetric {
    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        double maxReported = -1;
        double max = -1;
        for (WildcardPattern wildcardPattern : reportedHHH) {
            if (maxReported < wildcardPattern.getWeight()) {
                maxReported = wildcardPattern.getWeight();
            }
        }
        for (WildcardPattern wildcardPattern : hhh) {
            if (max < wildcardPattern.getWeight()) {
                max = wildcardPattern.getWeight();
            }
        }
        if (maxReported < 0) {
            return 0d;
        }
        return max / maxReported;
    }

    @Override
    public String toString() {
        return "MaxHHHRatio";
    }
}
