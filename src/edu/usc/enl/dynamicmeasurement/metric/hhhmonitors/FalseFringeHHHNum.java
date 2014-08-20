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
 * Time: 11:44 AM
 */
public class FalseFringeHHHNum extends HHHMonitorMetric {
    protected FalseHHHFinder falseHHHFinder;

    public FalseFringeHHHNum(FalseHHHFinder falseHHHFinder) {
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
                count++;
            }
        }
        return Double.valueOf(count);
    }

    @Override
    public String toString() {
        return "FalseFringeHHHNum";
    }

}
