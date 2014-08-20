package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/27/13
 * Time: 9:55 AM
 */
public class RecallDepthCount extends HHHMetric {
    private final int depth;

    public RecallDepthCount(int depth) {
        this.depth = depth;
    }

    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        int count = 0;
        int total = 0;
        for (WildcardPattern wildcardPattern : hhh) {
            if (wildcardPattern.getWildcardNum() == WildcardPattern.TOTAL_LENGTH - depth) {
                if (reportedHHH.contains(wildcardPattern)) {
                    count++;
                }
                total++;
            }
        }
        if (total == 0) {
            return null;
        }
        return 1.0 * count;
        // / total;
    }

    @Override
    public String toString() {
        return "Recall_" + depth;
    }
}
