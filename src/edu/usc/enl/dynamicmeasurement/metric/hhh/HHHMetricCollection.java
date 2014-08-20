package edu.usc.enl.dynamicmeasurement.metric.hhh;

import edu.usc.enl.dynamicmeasurement.metric.DropStatus;
import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.metric.NeedFolderMetric;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollectionInitializationException;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/27/13
 * Time: 9:52 PM
 */
public class HHHMetricCollection extends MetricCollection {

    private List<Metric> metrics;
    private String groundTruthRootFolder;

    public HHHMetricCollection(String groundTruthRootFolder) {
        this.groundTruthRootFolder = groundTruthRootFolder;
    }

    public static String findGroundTruthFolderFor(String groundTruthParentFolder, String f) {
        File[] folders = new File(groundTruthParentFolder).listFiles();
        if (folders == null) {
            System.err.println("Cannot open folder " + groundTruthParentFolder);
            System.exit(1);
        }

        for (File folder : folders) {
            if (folder.isDirectory()) {
                if (folder.getName().equals(f)) {
                    return (folder.getAbsolutePath());
//                    if (!useFilters) {
//                        return (folder.getAbsolutePath());
//                    }
//                    //must match filter
//                    if (filter == null) {
//                        return null;
//                    }
//                    if (folder.getName().contains(filter)) {
//                        return folder.getAbsolutePath();
//                    }
                }
            }
        }
        return null;
    }

    public static String findFilterFromFolderName(String folderName) {
        Pattern p = Pattern.compile("[01" + WildcardPattern.WILDCARD_FOLDER_CHAR + "]{32}");
        Matcher matcher = p.matcher(folderName);
        matcher.find();
        String group = matcher.group();
        if (group == null) {
            return null;
        }
        if (group.matches("^[01]*" + WildcardPattern.WILDCARD_FOLDER_CHAR + "*$")) {
            return group;
        }
        return null;
    }

    @Override
    public Map<Metric, SortedMap<Integer, Double>> runForFolder(File f) throws IOException {
        //first find real hhh folder
        String groundTruthFolder = findGroundTruthFolderFor(groundTruthRootFolder, f.getName());
        if (groundTruthFolder == null) {
            try {
                throw new MetricCollectionInitializationException("Ground truth folder for " + f.getAbsolutePath() + " is  not found");
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
            if (metric instanceof NeedFolderMetric) {
                ((NeedFolderMetric) metric).setFolder(f.getAbsolutePath());
            }
        }
        //need two files of HHH
        Map<Integer, List<WildcardPattern>> realHHHs = LoadWildcardPatterns(groundTruthFolder + "/hhh.csv");
        Map<Integer, List<WildcardPattern>> reportedHHHs = LoadWildcardPatterns(f.getAbsolutePath() + "/hhh.csv");

        MetricTask task = getTask(f.getAbsolutePath(), new MetricTask());
        MetricTask realTask = getTask(groundTruthFolder, new MetricTask());
        int dropTime = task.getDrop();

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
            for (Metric metric : metrics) {
                if (metric instanceof DropStatus) {
                    double drop = 0;
                    if (dropTime >= 0 && step > dropTime) {
                        drop = 1;
                    }
                    report.get(metric).put(step, drop);
                } else {
                    report.get(metric).put(step, ((HHHMetric) metric).compute(realHHH, reportedHHH, step, f.getName()));
                }
            }
        }
        return report;
    }

    protected void removeLast(Map<Integer, List<WildcardPattern>> realHHHs, Map<Integer, List<WildcardPattern>> reportedHHHs) {
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
        return new HHHMetricCollection(groundTruthRootFolder);
    }

    @Override
    public void init(File parentFolder) throws MetricCollectionInitializationException {
//        realHHHFolder = findRealHHHFolder(new File(groundTruthRootFolder + "/" + parentFolder.getName()));
//        if (realHHHFolder == null) {
//            throw new MetricCollectionInitializationException("Real HHH folder with name " + realHHHPrefix + " not found");
//        }
        metrics = new ArrayList<>();
        metrics.add(new Precision());
        metrics.add(new Recall());
//        metrics.add(new AncestorRecall());
//        metrics.add(new HHHNum());
//        metrics.add(new MaxHHHRatio());
//        metrics.add(new HHHWeight(false));
//        metrics.add(new MaxHHH(false));
//        metrics.add(new AncestorBytes());
//        metrics.add(new RecallBytes());
//        metrics.add(new AncestorError(true));
//        for (int i = 0; i < WildcardPattern.TOTAL_LENGTH + 1; i++) {
//            metrics.add(new RecallDepthCount(i));
//        }
        metrics.add(new DropStatus());
        metrics.add(new Utilization(parentFolder));
        Collections.sort(metrics, comparator);
    }

}

