package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 2/1/13
 * Time: 7:07 PM
 */
public class Recall extends HHHMetric {
    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        if (hhh.size() == 0) {
            return 1d;
        }
        int count = 0;
        for (WildcardPattern wildcardPattern : hhh) {
            if (reportedHHH.contains(wildcardPattern)) {
                count++;
            }
//            else {
//                System.out.println(wildcardPattern);
//            }
        }
        return 1.0 * count / hhh.size();
    }

    @Override
    public String toString() {
        return "Recall";
    }
}
