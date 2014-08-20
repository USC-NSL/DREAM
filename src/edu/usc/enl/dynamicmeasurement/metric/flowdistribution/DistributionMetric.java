package edu.usc.enl.dynamicmeasurement.metric.flowdistribution;

import edu.usc.enl.dynamicmeasurement.metric.Metric;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/12/2014
 * Time: 10:10 PM
 */
public abstract class DistributionMetric extends Metric {
    public abstract Double compute(Map<Integer, Integer> real, Map<Integer, Integer> reported, int step, String folder);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
