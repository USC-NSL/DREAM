package edu.usc.enl.dynamicmeasurement.metric.flowdistribution;

import edu.usc.enl.dynamicmeasurement.metric.DropStatus;
import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.metric.hhh.HHHMetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollectionInitializationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/12/2014
 * Time: 10:09 PM
 */
public class DistributionMetricCollection extends MetricCollection {

    private List<Metric> metrics;
    private String groundTruthRootFolder;

    public DistributionMetricCollection(String groundTruthRootFolder) {
        this.groundTruthRootFolder = groundTruthRootFolder;
    }

    @Override
    public Map<Metric, SortedMap<Integer, Double>> runForFolder(File f) throws IOException {
        String groundTruthFolder = HHHMetricCollection.findGroundTruthFolderFor(groundTruthRootFolder, f.getName());
        if (groundTruthFolder == null) {
            try {
                throw new MetricCollectionInitializationException("Ground truth folder for " + f.getAbsolutePath() + " is  not found");
            } catch (MetricCollectionInitializationException e) {
                throw new IOException(e);
            }
        }

        Map<Metric, SortedMap<Integer, Double>> report = new HashMap<>();
        for (Metric metric : metrics) {
            report.put(metric, new TreeMap<>());
        }

        Map<Integer, Map<Integer, Integer>> realDists = loadDistributions(groundTruthFolder + "/hhh.csv");
        Map<Integer, Map<Integer, Integer>> reportedDists = loadDistributions(f.getAbsolutePath() + "/hhh.csv");

        MetricTask task = getTask(f.getAbsolutePath(), new MetricTask());
        MetricTask realTask = getTask(groundTruthFolder, new MetricTask());
        int dropTime = task.getDrop();

        Map<Integer, Integer> reportedDist = null;
        for (int step = realTask.getStart(); step < realTask.getFinish() + 1; step++) {
            reportedDist = reportedDists.get(step);
            if (reportedDist == null) {
                reportedDist = Collections.EMPTY_MAP;
            }
            Map<Integer, Integer> realDist = realDists.get(step);
            if (realDist == null) {
                realDist = Collections.EMPTY_MAP;
            }
            for (Metric metric : metrics) {
                if (metric instanceof DropStatus) {
                    double drop = 0;
                    if (dropTime >= 0 && step > dropTime) {
                        drop = 1;
                    }
                    report.get(metric).put(step, drop);
                } else {
                    report.get(metric).put(step, ((DistributionMetric) metric).compute(realDist, reportedDist, step, f.getName()));
                }
            }
        }


        return report;
    }

    private Map<Integer, Map<Integer, Integer>> loadDistributions(String file) throws IOException {
        SortedMap<Integer, Map<Integer, Integer>> output = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int lineNo = 1;
            int lastTime = -1;
            Map<Integer, Integer> dist = null;
            while (br.ready()) {
                String line = br.readLine();
                StringTokenizer st = new StringTokenizer(line, ",");
                try {
                    int s = Integer.parseInt(st.nextToken());
                    if (lastTime != s) {
                        lastTime = s;
                        dist = new HashMap<>();
                        output.put(lastTime, dist);
                    }
                    int key = Integer.parseInt(st.nextToken());
                    int value = Integer.parseInt(st.nextToken());
                    if (dist.containsKey(key)) {
                        System.err.println("Duplicate key " + key + " at time " + lastTime + " in " + file + " line: " + line);
                        continue;
                    }
                    dist.put(key, value);
                } catch (Exception e) {
                    throw new RuntimeException("Incorrect file format in line " + lineNo + " of " + file, e);
                }
                lineNo++;
            }
        }
        return output;
    }

    @Override
    public List<Metric> getMetrics() {
        return metrics;
    }

    @Override
    public MetricCollection clone() {
        return new DistributionMetricCollection(groundTruthRootFolder);
    }

    @Override
    public void init(File parentFolder) throws MetricCollectionInitializationException {
        metrics = new ArrayList<>();
        metrics.add(new FlowNum());
        metrics.add(new WMRD());
        metrics.add(new DropStatus());
        Collections.sort(metrics, comparator);
    }
}
