package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 2/1/13
 * Time: 10:55 PM
 */
public class AncestorRecall extends HHHMetric {
    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        if (hhh.size() == 0) {
            return 1d;
        }
        double score = 0;
        for (WildcardPattern wildcardPattern : hhh) {
            wildcardPattern = wildcardPattern.clone();
            int i = 1;
            do {
                if (reportedHHH.contains(wildcardPattern)) {
                    score += 1.0 / i;
                    break;
                }
                if (wildcardPattern.canGoUp()) {
                    wildcardPattern = wildcardPattern.goUp();
                    i <<= 1;
                }
            } while (wildcardPattern.canGoUp());
        }
        return 1.0 * score / hhh.size();
    }

    @Override
    public String toString() {
        return "AncestorRecall";
    }
}
