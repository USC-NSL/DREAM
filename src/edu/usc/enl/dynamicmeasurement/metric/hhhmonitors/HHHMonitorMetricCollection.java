package edu.usc.enl.dynamicmeasurement.metric.hhhmonitors;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.singleswitch.FalseHHHFinder;
import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.metric.hhh.HHHMetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.hhh.NeedGroundTruthFolder;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollectionInitializationException;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/12/13
 * Time: 8:45 AM
 */
public class HHHMonitorMetricCollection extends MetricCollection {
    private final FalseHHHFinder falseHHHFinder;
    private List<Metric> metrics;
    private String groundTruthRootFolder;
    private String groundTruthParentFolder;

    public HHHMonitorMetricCollection(String groundTruthRootFolder, boolean refine) {
        this.groundTruthRootFolder = groundTruthRootFolder;
        falseHHHFinder = new FalseHHHFinder(refine, 0);
    }

    @Override
    public Map<Metric, SortedMap<Integer, Double>> runForFolder(File f) throws IOException {
        String groundTruthFolder = HHHMetricCollection.findGroundTruthFolderFor(groundTruthParentFolder, f.getName());
        if (groundTruthFolder == null) {
            try {
                throw new MetricCollectionInitializationException("Ground truth folder for " + f.getAbsolutePath() + " is not found");
            } catch (MetricCollectionInitializationException e) {
                throw new IOException(e);
            }
        }

        Map<Metric, SortedMap<Integer, Double>> report = new HashMap<>();
        for (Metric metric : metrics) {
            report.put(metric, new TreeMap<Integer, Double>());
            if (metric instanceof NeedGroundTruthFolder) {
                ((NeedGroundTruthFolder) metric).setGroundTruthFolder(groundTruthFolder);
            }
        }
        //need two files of HHH and one montiros
        Map<Integer, List<WildcardPattern>> realHHHs = LoadWildcardPatterns(groundTruthFolder + "/hhh.csv");
        Map<Integer, List<WildcardPattern>> reportedHHHs = LoadWildcardPatterns(f.getAbsolutePath() + "/hhh.csv");
        Map<Integer, List<WildcardPattern>> monitors = LoadWildcardPatterns(f.getAbsolutePath() + "/monitors.csv");

        ThresholdMetricTask task = (ThresholdMetricTask) getTask(f.getAbsolutePath(), new ThresholdMetricTask());

        List<WildcardPattern> reportedHHH = null;
        for (Map.Entry<Integer, List<WildcardPattern>> realHHH : realHHHs.entrySet()) {
//            if (realHHH.getKey()==34){
//                System.out.println();
//            }
            reportedHHH = reportedHHHs.get(realHHH.getKey());
            if (reportedHHH == null) {
                reportedHHH = Collections.EMPTY_LIST;
            }
            falseHHHFinder.findFalseHHHs(reportedHHH, monitors.get(realHHH.getKey() + 1), task.getThreshold());
            for (Metric metric : metrics) {
                report.get(metric).put(realHHH.getKey(), ((HHHMonitorMetric) metric).compute(realHHH.getValue(), reportedHHH, monitors.get(realHHH.getKey() + 1)));
            }
            int i = 0;
        }
        return report;
    }

    private double getThresholdValueOutOfPercent(Map<Integer, List<WildcardPattern>> monitors, double th) {
        double max = -1;
        for (List<WildcardPattern> wildcardPatterns : monitors.values()) {
            double sum = 0;
            for (WildcardPattern wildcardPattern : wildcardPatterns) {
                sum += wildcardPattern.getWeight();
            }
            if (max < sum) {
                max = sum;
            }
        }
        return max * th;
    }

    private void removeLast(Map<Integer, List<WildcardPattern>> realHHHs, Map<Integer, List<WildcardPattern>> reportedHHHs,
                            Map<Integer, List<WildcardPattern>> monitors) {
        int max = -1;
        for (Integer s : realHHHs.keySet()) {
            max = Math.max(s, max);
        }
        realHHHs.remove(max);
        reportedHHHs.remove(max);
    }

    @Override
    public List<Metric> getMetrics() {
        return metrics;
    }

    @Override
    public MetricCollection clone() {
        return new HHHMonitorMetricCollection(groundTruthRootFolder, falseHHHFinder.isRefine());
    }

    @Override
    public void init(File parentFolder) throws MetricCollectionInitializationException {
        if (groundTruthRootFolder == null) {
            groundTruthParentFolder = parentFolder.getAbsolutePath();
        } else {
            groundTruthParentFolder = groundTruthRootFolder;//+ "/" + parentFolder.getName();
        }
        metrics = new ArrayList<>();
        metrics.add(new TrueHHHMonitorMetric(false, falseHHHFinder));
        metrics.add(new RealFalseHHHMonitorMetric(false, falseHHHFinder));
        metrics.add(new DescribedTrueTraffic(false, falseHHHFinder));
        metrics.add(new DescribedTrueTraffic(true, falseHHHFinder));
        metrics.add(new RealHHHNum());
        metrics.add(new DetectedHHHNum());
        metrics.add(new FalseFringeHHHWeight(falseHHHFinder));
        metrics.add(new RealFalseFringeHHHMonitorMetric(false, falseHHHFinder));
        metrics.add(new FalseHHHWeight(falseHHHFinder));
        metrics.add(new FalseFringeHHHNum(falseHHHFinder));
        Collections.sort(metrics, comparator);
    }

}

