package edu.usc.enl.dynamicmeasurement.metric.hhhmonitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/12/13
 * Time: 11:02 AM
 */
public class RealHHHNum extends HHHMonitorMetric{
    @Override
    public Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        return Double.valueOf(hhh.size());
    }

    @Override
    public String toString() {
        return "RealHHHNum";
    }
}
