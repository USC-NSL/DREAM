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
 * Time: 9:09 AM
 */
public class RealFalseHHHMonitorMetric extends TrueHHHMonitorMetric {
    public RealFalseHHHMonitorMetric(boolean ratio, FalseHHHFinder falseHHHFinder) {
        super(ratio, falseHHHFinder);
    }

    public Double compute(List<WildcardPattern> realHhhs, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        List<WildcardPattern> falseHHHs = falseHHHFinder.getLastFalseHHHs();
        Set<WildcardPattern> realHHHsSet = new HashSet<>();
        realHHHsSet.addAll(realHhhs);
        double falseRealHHHs = 0;
        for (WildcardPattern falseHHH : falseHHHs) {
            if (realHHHsSet.contains(falseHHH)) {
                falseRealHHHs++;
            }
        }
//        System.out.println("Real internal HHH");
//        for (WildcardPattern falseHHH : falseHHHs) {
//            if (realHHHsSet.contains(falseHHH) && !monitors.contains(falseHHH)) {
//                System.out.println(falseHHH);
//            }
//        }
        if (ratio) {
            return falseRealHHHs / reportedHHH.size();
        } else {
            return falseRealHHHs;
        }
    }

    @Override
    public String toString2() {
        return "RealFalseHHHMonitorMetric";
    }
}
