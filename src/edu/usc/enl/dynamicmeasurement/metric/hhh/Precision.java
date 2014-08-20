package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 2/1/13
 * Time: 6:36 PM
 */
public class Precision extends HHHMetric {
    private int step = 0;

    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        if (reportedHHH.size() == 0) {
            return 1d;
        }
        int count = 0;
        for (WildcardPattern wildcardPattern : reportedHHH) {
            if (hhh.contains(wildcardPattern)) {
                count++;
            }
//            else {
//                System.out.println(step + "," + wildcardPattern.toStringNoWeight() + "," + wildcardPattern.getWeight());
//            }
        }
        this.step++;
        return 1.0 * count / reportedHHH.size();
    }

    @Override
    public String toString() {
        return "Precision";
    }
}
