package edu.usc.enl.dynamicmeasurement.metric.hhhmonitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/12/13
 * Time: 11:01 AM
 */
public class DetectedHHHNum extends HHHMonitorMetric {
    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        return Double.valueOf(reportedHHH.size());
    }

    @Override
    public String toString() {
        return "DetectedHHHNum";
    }
}
