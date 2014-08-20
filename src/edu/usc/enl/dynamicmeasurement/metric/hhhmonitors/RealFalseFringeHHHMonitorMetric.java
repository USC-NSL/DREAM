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
 * Time: 11:26 AM
 */
public class RealFalseFringeHHHMonitorMetric extends TrueHHHMonitorMetric {

    public RealFalseFringeHHHMonitorMetric(boolean ratio, FalseHHHFinder falseHHHFinder) {
        super(ratio, falseHHHFinder);
    }

    public Double compute(List<WildcardPattern> realHhhs, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        List<WildcardPattern> falseHHHs = falseHHHFinder.getLastFalseHHHs();
        Set<WildcardPattern> realHHHsSet = new HashSet<>();
        Set<WildcardPattern> monitorsSet = new HashSet<>(monitors);
        realHHHsSet.addAll(realHhhs);
        double falseRealHHHs = 0;
//        System.out.println("Fringe");
//        for (WildcardPattern falseHHH : falseHHHs) {
//            if (monitorsSet.contains(falseHHH)) {
//                System.out.println(falseHHH);
//            }
//        }
//        System.out.println("Real fringe");
        for (WildcardPattern falseHHH : falseHHHs) {
            if (realHHHsSet.contains(falseHHH) && monitorsSet.contains(falseHHH)) {
                falseRealHHHs++;
//                System.out.println(falseHHH);
            }
        }
        if (ratio) {
            return falseRealHHHs / reportedHHH.size();
        } else {
            return falseRealHHHs;
        }
    }

    @Override
    public String toString2() {
        return "RealFalseFringeHHHMonitorMetric";
    }


}
