package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 6/28/13
 * Time: 10:22 PM
 */
public class MonitorWeightMedian extends MonitorMetric {

    public MonitorWeightMedian() {
    }

    @Override
    public Double compute(List<WildcardPattern> monitors) {
        if (monitors.size() == 0) {
            return null;
        }
        Collections.sort(monitors, WildcardPattern.WEIGHT_COMPARATOR);
        if (monitors.size() % 2 == 0) {
            return (monitors.get(monitors.size() / 2 - 1).getWeight() + monitors.get(monitors.size() / 2).getWeight()) / 2;
        }
        return monitors.get(monitors.size() / 2).getWeight();

    }

    @Override
    public String toString() {
        return "MonitorWeight_Median" ;
    }
}
