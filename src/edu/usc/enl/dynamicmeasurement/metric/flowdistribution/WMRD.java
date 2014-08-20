package edu.usc.enl.dynamicmeasurement.metric.flowdistribution;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/12/2014
 * Time: 10:10 PM
 */
public class WMRD extends DistributionMetric {
    @Override
    public Double compute(Map<Integer, Integer> real, Map<Integer, Integer> reported, int step, String folder) {
        double nominator = 0;
        double denominator = 0;
        for (Map.Entry<Integer, Integer> realEntry : real.entrySet()) {
            Integer reportedFreq = reported.get(realEntry.getKey());
            if (reportedFreq == null) {
                reportedFreq = 0;
            }
            Integer realFreq = realEntry.getValue();
            nominator += Math.abs(reportedFreq - realFreq);
            denominator += reportedFreq + realFreq;
        }
        for (Map.Entry<Integer, Integer> reportedEntry : reported.entrySet()) {
            if (!real.containsKey(reportedEntry.getKey())) {
                nominator += reportedEntry.getValue();
                denominator += reportedEntry.getValue();
            }
        }
        denominator /= 2;
        return nominator / denominator;
    }
}
