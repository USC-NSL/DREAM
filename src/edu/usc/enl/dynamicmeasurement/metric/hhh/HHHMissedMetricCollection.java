package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.metric.NeedFolderMetric;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollectionInitializationException;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 1/30/14
 * Time: 8:29 PM
 */
public class HHHMissedMetricCollection extends MetricCollection {
    private final String groundTruthRootFolder;
    private Metric meanFound;
    private Metric meanNotFound;
    private Metric medianFound;
    private Metric medianNotFound;
    private HHHMetric accuracyMetric;
    private double accuracyBound = 0.8;

    public HHHMissedMetricCollection(String groundTruthRootFolder) {
        this.groundTruthRootFolder = groundTruthRootFolder;
    }

    public Map<Metric, SortedMap<Integer, Double>> runForFolder(File f) throws IOException {
        //first find real hhh folder
        String groundTruthFolder = HHHMetricCollection.findGroundTruthFolderFor(groundTruthRootFolder, f.getName());
        if (groundTruthFolder == null) {
            try {
                throw new MetricCollectionInitializationException("Ground truth folder for " + f.getAbsolutePath() + " is  not found");
            } catch (MetricCollectionInitializationException e) {
                throw new IOException(e);
            }
        }

        Map<Metric, SortedMap<Integer, Double>> report = new HashMap<>();
        {
            Metric metric = accuracyMetric;
            if (metric instanceof NeedGroundTruthFolder) {
                ((NeedGroundTruthFolder) metric).setGroundTruthFolder(groundTruthFolder);
            }
            if (metric instanceof NeedFolderMetric) {
                ((NeedFolderMetric) metric).setFolder(f.getAbsolutePath());
            }
        }
        for (Metric metric : getMetrics()) {
            report.put(metric, new TreeMap<Integer, Double>());
        }
        //need two files of HHH
        Map<Integer, List<WildcardPattern>> realHHHs = LoadWildcardPatterns(groundTruthFolder + "/hhh.csv");
        Map<Integer, List<WildcardPattern>> reportedHHHs = LoadWildcardPatterns(f.getAbsolutePath() + "/hhh.csv");

        MetricTask task = getTask(f.getAbsolutePath(), new MetricTask());
        MetricTask realTask = getTask(groundTruthFolder, new MetricTask());

        List<WildcardPattern> reportedHHH = null;
        for (int step = realTask.getStart(); step < realTask.getFinish() + 1; step++) {
            reportedHHH = reportedHHHs.get(step);
            if (reportedHHH == null) {
                reportedHHH = Collections.EMPTY_LIST;
            }
            List<WildcardPattern> realHHH = realHHHs.get(step);
            if (realHHH == null) {
                realHHH = Collections.EMPTY_LIST;
            }
            Double accuracy = accuracyMetric.compute(realHHH, reportedHHH, step, f.getName());
            if (accuracy != null && accuracy >= accuracyBound && accuracy < 1) {
                // find mean and median of found and missed HHHs
                List<WildcardPattern> found = new ArrayList<>();
                List<WildcardPattern> notFound = new ArrayList<>();

                for (WildcardPattern wildcardPattern : realHHH) {
                    if (reportedHHH.contains(wildcardPattern)) {
                        found.add(wildcardPattern);
                    } else {
                        notFound.add(wildcardPattern);
                    }
                }

                double meanFound = mean(found);
                double meanNotFound = mean(notFound);
                double medianFound = median(found);
                double medianNotFound = median(notFound);
                report.get(this.meanFound).put(step, meanFound);
                report.get(this.meanNotFound).put(step, meanNotFound);
                report.get(this.medianFound).put(step, medianFound);
                report.get(this.medianNotFound).put(step, medianNotFound);
            } else {
                report.get(this.meanFound).put(step, 0d);
                report.get(this.meanNotFound).put(step, 0d);
                report.get(this.medianFound).put(step, 0d);
                report.get(this.medianNotFound).put(step, 0d);
            }
        }
        return report;
    }

    private double median(List<WildcardPattern> found) {
        Collections.sort(found, WildcardPattern.WEIGHT_COMPARATOR);
        int size = found.size();
        if (size % 2 == 0) {
            return (found.get(size / 2 - 1).getWeight() + found.get(size / 2).getWeight()) / 2;
        } else {
            return found.get(size / 2).getWeight();
        }
    }

    private double mean(List<WildcardPattern> found) {
        double mean = 0;
        for (WildcardPattern wildcardPattern : found) {
            mean += wildcardPattern.getWeight();
        }
        return mean / found.size();
    }

    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(meanFound, meanNotFound, medianFound, medianNotFound);
    }

    @Override
    public MetricCollection clone() {
        return new HHHMissedMetricCollection(groundTruthRootFolder);
    }

    @Override
    public void init(File parentFolder) throws MetricCollectionInitializationException {
        accuracyMetric = new Recall();
        meanFound = new Metric.DummyMetric("MeanFound");
        meanNotFound = new Metric.DummyMetric("MeanNotFound");
        medianFound = new Metric.DummyMetric("MedianFound");
        medianNotFound = new Metric.DummyMetric("MedianNotFound");

    }
}
