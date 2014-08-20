package edu.usc.enl.dynamicmeasurement.metric.flowdistribution;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/12/2014
 * Time: 10:18 PM
 */
public class FlowNum extends DistributionMetric {
    @Override
    public Double compute(Map<Integer, Integer> real, Map<Integer, Integer> reported, int step, String folder) {
        return (double) real.values().stream().mapToInt(Integer::intValue).sum();
    }
}
