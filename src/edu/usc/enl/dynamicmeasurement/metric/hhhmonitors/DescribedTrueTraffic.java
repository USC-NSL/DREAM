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
 * Time: 9:31 AM
 */
public class DescribedTrueTraffic extends TrueHHHMonitorMetric {
    public DescribedTrueTraffic(boolean ratio,FalseHHHFinder falseHHHFinder) {
        super(ratio, falseHHHFinder);
    }

    public Double compute(List<WildcardPattern> realHhhs, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        Set<WildcardPattern> falseHHHs = new HashSet<>(falseHHHFinder.getLastFalseHHHs());
        double describedTrueHHHsTraffic = 0;
        for (WildcardPattern hhh : reportedHHH) {
            if (!falseHHHs.contains(hhh)) {
                describedTrueHHHsTraffic += hhh.getWeight();
            }
        }
        if (ratio) {
            double sum = 0;
            for (WildcardPattern monitor : monitors) {
                sum += monitor.getWeight();
            }
            return describedTrueHHHsTraffic / sum;
        } else {
            return describedTrueHHHsTraffic;
        }
    }

    @Override
    public String toString2() {
        return "DescribedTrueTraffic";
    }
}
