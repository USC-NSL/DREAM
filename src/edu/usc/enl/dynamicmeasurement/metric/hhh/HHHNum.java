package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 2/27/13
 * Time: 11:29 PM
 */
public class HHHNum extends HHHMetric {
    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, int step, String folder) {
        if (hhh.size() == 0) {
            return 1.0;
        }
        return 1.0 * reportedHHH.size() / hhh.size();
    }

    @Override
    public String toString() {
        return "HHHNum";
    }
}
