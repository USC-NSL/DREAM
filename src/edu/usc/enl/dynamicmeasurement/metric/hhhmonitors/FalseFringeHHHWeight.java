package edu.usc.enl.dynamicmeasurement.metric.hhhmonitors;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.singleswitch.FalseHHHFinder;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/12/13
 * Time: 11:01 AM
 */
public class FalseFringeHHHWeight extends HHHMonitorMetric {
    protected FalseHHHFinder falseHHHFinder;

    public FalseFringeHHHWeight(FalseHHHFinder falseHHHFinder) {
        this.falseHHHFinder = falseHHHFinder;
    }

    @Override
    public Double compute(List<WildcardPattern> realHhhs, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        List<WildcardPattern> falseHHHs = falseHHHFinder.getLastFalseHHHs();
        Set<WildcardPattern> monitorsSet = new HashSet<>(monitors);
        double falseFringeHHHsWeight = 0;
        int count = 0;
        for (WildcardPattern falseHHH : falseHHHs) {
            if (monitorsSet.contains(falseHHH)) {
                falseFringeHHHsWeight += falseHHH.getWeight();
                count++;
            }
        }
        if (count != 0) {
            return falseFringeHHHsWeight / count;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "FalseFringeHHHWeight";
    }
}
