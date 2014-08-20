package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/28/13
 * Time: 7:34 AM
 */
public class RecallBytes extends HHHMetric {
    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        if (hhh.size() == 0) {
            return 1d;
        }
        double sum = 0;
        double sumAll = 0;
        for (WildcardPattern wildcardPattern : hhh) {
            if (reportedHHH.contains(wildcardPattern)) {
                sum += wildcardPattern.getWeight();
            }
            sumAll += wildcardPattern.getWeight();
        }
        return sum / sumAll;
    }

    @Override
    public String toString() {
        return "RecallBytes";
    }
}
