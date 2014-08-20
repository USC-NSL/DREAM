package edu.usc.enl.dynamicmeasurement.metric.hhhmonitors;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.singleswitch.FalseHHHFinder;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/12/13
 * Time: 8:57 AM
 */
public class TrueHHHMonitorMetric extends HHHMonitorMetric {
    protected final boolean ratio;
    protected final FalseHHHFinder falseHHHFinder;


    public TrueHHHMonitorMetric(boolean ratio, FalseHHHFinder falseHHHFinder) {
        this.ratio = ratio;
        this.falseHHHFinder = falseHHHFinder;
    }

    @Override
    public Double compute(List<WildcardPattern> realHhhs, List<WildcardPattern> reportedHHH, List<WildcardPattern> monitors) {
        //find reported hhhs that have descendent monitors or true descendent hhhs
        List<WildcardPattern> falseHHHs = falseHHHFinder.getLastFalseHHHs();
        if (ratio) {
            return ((double) (reportedHHH.size() - falseHHHs.size())) / reportedHHH.size();
        } else {
            return (double) (reportedHHH.size() - falseHHHs.size());
        }
    }


    @Override
    public String toString() {
        return toString2() + (ratio ? "_ratio" : "");
    }

    protected String toString2() {
        return "TrueHHHMonitorMetric";
    }

}
