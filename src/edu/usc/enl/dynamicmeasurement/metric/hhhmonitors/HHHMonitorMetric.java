package edu.usc.enl.dynamicmeasurement.metric.hhhmonitors;

import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/12/13
 * Time: 8:51 AM
 */
public abstract class HHHMonitorMetric extends Metric {
    public abstract Double compute(List<WildcardPattern> hhh, List<WildcardPattern> reportedHHH,
                                   List<WildcardPattern> monitors);
}
