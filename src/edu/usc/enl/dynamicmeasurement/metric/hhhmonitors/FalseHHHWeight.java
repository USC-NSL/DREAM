package edu.usc.enl.dynamicmeasurement.metric.hhhmonitors;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.singleswitch.FalseHHHFinder;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/12/13
 * Time: 12:33 PM
 */
public class FalseHHHWeight extends HHHMonitorMetric {
    protected FalseHHHFinder falseHHHFinder;

    public FalseHHHWeight(FalseHHHFinder falseHHHFinder) {
        this.falseHHHFinder = falseHHHFinder;
    }

    @Override
    public Double compute(List<WildcardPattern> realHhhs, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        List<WildcardPattern> falseHHHs = falseHHHFinder.getLastFalseHHHs();
        int count = 0;
        int sum = 0;
        for (WildcardPattern falseHHH : falseHHHs) {
            sum += falseHHH.getWeight();
            count++;
        }
        if (count != 0) {
            return Double.valueOf(sum / count);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "FalseHHHWeight";
    }
}
