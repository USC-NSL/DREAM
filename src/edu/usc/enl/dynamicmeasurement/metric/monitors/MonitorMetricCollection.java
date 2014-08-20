package edu.usc.enl.dynamicmeasurement.metric.monitors;

import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollectionInitializationException;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/27/13
 * Time: 10:12 PM
 */
public class MonitorMetricCollection extends MetricCollection {
    private List<Metric> metrics;

    @Override
    public Map<Metric, SortedMap<Integer, Double>> runForFolder(File f) throws IOException {
        Map<Metric, SortedMap<Integer, Double>> report = new HashMap<>();
        for (Metric metric : metrics) {
            report.put(metric, new TreeMap<Integer, Double>());
        }
        //need two files of HHH
        Map<Integer, List<WildcardPattern>> reportedHHHs = LoadWildcardPatterns(f.getAbsolutePath() + "/monitors.csv");

        for (Map.Entry<Integer, List<WildcardPattern>> reportedHHH : reportedHHHs.entrySet()) {
            for (Metric metric : metrics) {
                report.get(metric).put(reportedHHH.getKey(), ((MonitorMetric) metric).compute(reportedHHH.getValue()));
            }
        }
        return report;
    }

    protected void removeLast(Map<Integer, List<WildcardPattern>> reportedHHHs) {
        int max = -1;
        for (Integer s : reportedHHHs.keySet()) {
            max = Math.max(s, max);
        }
        reportedHHHs.remove(max);
    }

    @Override
    public List<Metric> getMetrics() {
        return metrics;
    }

    @Override
    public MetricCollection clone() {
        return new MonitorMetricCollection();
    }

    @Override
    public void init(File parentFolder) throws MetricCollectionInitializationException {
        metrics = new ArrayList<>();
        metrics.add(new MonitorsDepth());
        metrics.add(new MonitorsNum());
        metrics.add(new MonitorWeight(false));
        //metrics.add(new MonitorWeight(true));
        metrics.add(new MonitorWeightMax(false));
        metrics.add(new MonitorWeightMax(true));
        //metrics.add(new MonitorWeightMedian());
        for (int i = 0; i < WildcardPattern.TOTAL_LENGTH + 1; i++) {
            metrics.add(new MonitorDepthCount(i));
        }
        Collections.sort(metrics, comparator);
    }
}
