package edu.usc.enl.dynamicmeasurement.metric;

import edu.usc.enl.dynamicmeasurement.metric.hhh.HHHMetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollection;
import edu.usc.enl.dynamicmeasurement.metric.metriccollection.MetricCollectionInitializationException;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.multithread.MultiThread;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 2/1/13
 * Time: 7:19 PM
 */
public class ComputeMetricsScript {
    private static final int warmupEntries = 0;
    private final MetricCollection metricCollection;
    private final String outputFileName;
    private final boolean writeTimeSeries;

    public ComputeMetricsScript(MetricCollection metricCollection, String outputFileName, boolean writeTimeSeries) {
        this.metricCollection = metricCollection;
        this.outputFileName = outputFileName;
        this.writeTimeSeries = writeTimeSeries;
    }

    public static void main(String[] args) throws Exception {
        String rootDir = "output";
        boolean writeTimeSeries = false;
        boolean useFilters = false;
        String groundTruthFolder = rootDir;
        int level = 2;
        String[] levelRegX = null;
        double threshold = 0.01;

        Options options = new Options();
        options.addOption(new Option("h", false, "Shows this help"));
        options.addOption(OptionBuilder.withArgName("Folder").hasArg().isRequired().withDescription("Input folder").create('i'));
        options.addOption(OptionBuilder.withArgName("Pattern").hasArg().withDescription("Use regular expression for first level").create("regx"));
        options.addOption(OptionBuilder.withArgName("Level").withType(Number.class).hasArg().withDescription("Levels of files").create('l'));
        options.addOption(OptionBuilder.withDescription("Write time series of metrics (false)").create('t'));
        options.addOption(OptionBuilder.withDescription("Match filter from folder name for ground-truth (false)").create('f'));
        options.addOption(OptionBuilder.hasArg().withArgName("folder").isRequired().withDescription("Ground truth parent folder (same as input)").create('g'));
//        options.addOption(OptionBuilder.hasArg().withArgName(ConfigReader.PROPERTY_VALUE).isRequired().withType(Number.class).withDescription("Threshold value)").create("threshold"));

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h") || cmd.getOptions().length < options.getRequiredOptions().size()) {
                showHelpExit(options);
            }
            rootDir = cmd.getOptionValue('i');
            writeTimeSeries = cmd.hasOption('t');
            useFilters = cmd.hasOption('f');
            groundTruthFolder = cmd.getOptionValue('g');
            String levelRegXS = cmd.getOptionValue("regx");
            if (levelRegXS != null) {
                levelRegX = levelRegXS.split(",");
            }
            String levelS = cmd.getOptionValue('l');
            if (levelS != null) {
                level = Integer.parseInt(levelS);
            }
        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            showHelpExit(options);
        }

        WildcardPattern.TOTAL_LENGTH = 32;

        {
            HHHMetricCollection metricCollection1 = new HHHMetricCollection(groundTruthFolder);
            ComputeMetricsScript computeMetricsScript = new ComputeMetricsScript(
                    metricCollection1,
                    "HHHMetrics.csv", writeTimeSeries);
            computeMetricsScript.runForEachMatchingFolder(rootDir, level - 1, levelRegX);
        }

//        {
//            DistributionMetricCollection metricCollection1 = new DistributionMetricCollection(groundTruthFolder);
//            ComputeMetricsScript computeMetricsScript = new ComputeMetricsScript(
//                    metricCollection1,
//                    "FSMetrics.csv", writeTimeSeries);
//            computeMetricsScript.runForEachMatchingFolder(rootDir, level - 1, levelRegX);
//        }

//        {
//            HHHMissedMetricCollection metricCollection1 = new HHHMissedMetricCollection(groundTruthFolder);
//            ComputeMetricsScript computeMetricsScript = new ComputeMetricsScript(
//                    metricCollection1,
//                    "HHHMisedMetrics.csv", writeTimeSeries);
//            computeMetricsScript.runForEachMatchingFolder(rootDir, level - 1, levelRegX);
//        }

//        {
//            MonitorMetricCollection metricCollection1 = new MonitorMetricCollection();
//            ComputeMetricsScript computeMetricsScript = new ComputeMetricsScript(
//                    metricCollection1,
//                    "MonitorMetrics.csv", writeTimeSeries);
//            computeMetricsScript.runForEachMatchingFolder(rootDir, level - 1, levelRegX);
//        }
//
//        {
//            HHHMonitorMetricCollection metricCollection1 = new HHHMonitorMetricCollection(groundTruthFolder, false);
//            ComputeMetricsScript computeMetricsScript = new ComputeMetricsScript(
//                    metricCollection1,
//                    "HHHMonitorMetrics.csv", writeTimeSeries);
//            computeMetricsScript.runForEachMatchingFolder(rootDir, level - 1, levelRegX);
//        }
//        {
//            HHHMonitorMetricCollection metricCollection1 = new HHHMonitorMetricCollection(groundTruthFolder, true);
//            ComputeMetricsScript computeMetricsScript = new ComputeMetricsScript(
//                    metricCollection1,
//                    "HHHMonitorMetrics_refine.csv", writeTimeSeries);
//            computeMetricsScript.runForEachMatchingFolder(rootDir, level - 1, levelRegX);
//        }

    }

    //    private static List<String> getFiles(String grandParentFolder, String pattern) {
//        File[] files = new File(grandParentFolder).listFiles();
//        List<String> output = new ArrayList<>();
//        for (File file : files) {
//            if (file.isDirectory() && file.getName().matches(pattern))
//                output.add(file.getAbsolutePath() + "/");
//        }
//        return output;
//    }
    protected static void showHelpExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java <classname>", options, true);
        System.exit(0);
    }

    protected static double getMean(Collection<Double> values) {
        if (values.size() == 0) {
            return 0;
        }
        double sum = 0;
        int count = 0;
        for (Double value : values) {
            if (value != null) {
                sum += value;
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        return sum / count;
    }

    protected static double getVar(Collection<Double> values, double mean) {
        if (values.size() == 0) {
            return 0;
        }
        double sum = 0;
        int count = 0;
        for (Double value : values) {
            if (value != null) {
                double v = value - mean;
                sum += v * v;
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        return sum / count;
    }

    protected void runForEachMatchingFolder(String rootDir, int level, String[] regX) throws Exception {
        File file = new File(rootDir);
        if (!file.exists()) {
            System.err.println("Folder " + file.getAbsolutePath() + " does not exist!");
            System.exit(1);
        }
        if (level == 0) {
            try {
                System.out.println(file);
                computeForFolder(file, (validRegX(level, regX) ? regX[level] : null));
            } catch (MetricCollectionInitializationException e) {
                System.err.println(e.getMessage());
            }
        } else {
            for (File parentFolder : file.listFiles()) {
                if (!parentFolder.isDirectory()) {
                    continue;
                }
                if (validRegX(level, regX) && !parentFolder.getName().matches(regX[level])) {
                    continue;
                }

                runForEachMatchingFolder(parentFolder.getAbsolutePath(), level - 1, regX);
            }
        }

    }

    private boolean validRegX(int level, String[] regX) {
        return regX.length > level && regX[level].length() > 0;
    }

    protected void computeForFolder(File parentFolder, final String lastRegX) throws Exception {

        File[] files0 = parentFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return lastRegX == null || name.matches(lastRegX);
            }
        });
        if (files0 == null) {
            System.err.println("folder " + parentFolder + " not found");
            return;
        }
//        new ConfigReader().read(parentFolder + "/config.xml");

        metricCollection.init(parentFolder);

        List<File> folders = new ArrayList<>();
        for (File file : files0) {
            if (file.isDirectory()) {
                folders.add(file);
            }
        }

        Collections.sort(folders, FolderNameComparator());

        computeAndPrintMetricsParallel(folders, parentFolder + "/" + outputFileName);
    }

    protected Comparator<File> FolderNameComparator() {
        return new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                if (name1.replaceAll("\\d", "").equals(name2.replaceAll("\\d", ""))) {
                    Matcher m1 = Pattern.compile("\\d+").matcher(name1);
                    Matcher m2 = Pattern.compile("\\d+").matcher(name2);
                    while (m1.find()) {
                        String g1 = m1.group();
                        m2.find();
                        String g2 = m2.group();
                        int c = new Double(g1).compareTo(new Double(g2));
                        if (c != 0) {
                            return c;
                        }
                    }
                    return 0;
                } else {
                    return name1.compareTo(name2);
                }
            }
        };
    }

    public void printMeanVarHeader(List<Metric> sortedMetrics, PrintWriter pw) {
        for (Metric metric : sortedMetrics) {
            pw.print("," + metric + "_Mean");
            pw.print("," + metric + "_Var ");
        }
        pw.println();
    }

    protected void computeAndPrintMetricsParallel(List<File> folders, String outputFile) throws IOException {
        MultiThread multiThread = new MultiThread(6);
        List<MapCallable> tasks = new ArrayList<>(folders.size());
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputFile))))) {
            final List<Metric> sortedMetrics = metricCollection.getMetrics();

            for (final File reportedDir : folders) {
                if (reportedDir.isDirectory()) {
                    MapCallable task = new MapCallable(metricCollection, reportedDir, sortedMetrics);
                    tasks.add(task);
                    multiThread.offer(task);
                }
            }
            printMeanVarHeader(sortedMetrics, pw);
            multiThread.runJoin();
            for (MapCallable task : tasks) {
                MetricsReport metricsReport = task.getMetricsReport();
                String subDirName = metricsReport.getReportedDir().getName();
                pw.print(subDirName);
                //Compute the result
                dropWarmUpEntriesAndLast(metricsReport.getReport(), warmupEntries);
                Map<Metric, List<Double>> hhhMetricMeanVar = computeMeanVar(metricsReport.getReport());
                printMetrics(sortedMetrics, hhhMetricMeanVar, pw);
            }
        } finally {
            multiThread.finishThreads();
        }
    }


    protected void computeAndPrintMetricsParallel2(List<File> folders, String outputFile) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputFile))))) {
            final List<Metric> sortedMetrics = metricCollection.getMetrics();


            CompletionService<MetricsReport> pool = new ExecutorCompletionService<MetricsReport>(threadPool);
            int num = 0;
            for (final File reportedDir : folders) {
                if (reportedDir.isDirectory()) {
                    num++;
//                    MetricCollection metricCollection1 = (MetricCollection) metricCollection.clone();
//                    metricCollection.init(reportedDir.getParentFile());
                    pool.submit(new MapCallable(metricCollection, reportedDir, sortedMetrics));
                }
            }
            printMeanVarHeader(sortedMetrics, pw);
            for (int i = 0; i < num; i++) {
                MetricsReport metricsReport = pool.take().get();
                String subDirName = metricsReport.getReportedDir().getName();
                pw.print(subDirName);
                //Compute the result
                dropWarmUpEntriesAndLast(metricsReport.getReport(), warmupEntries);
                Map<Metric, List<Double>> hhhMetricMeanVar = computeMeanVar(metricsReport.getReport());
                printMetrics(sortedMetrics, hhhMetricMeanVar, pw);
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

//    protected void computeAndPrintMetrics(MetricCollection metricCollection,
//                                          List<File> folders, String outputFile) throws IOException {
//        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputFile))))) {
//            List<Metric> sortedMetrics = metricCollection.getMetrics();
//            printMeanVarHeader(sortedMetrics, pw);
//            for (File reportedDir : folders) {
//                if (reportedDir.isDirectory()) {
//                    String subDirName = reportedDir.getName();
//                    pw.print(subDirName);
//
//                    try {
//                        Map<Metric, SortedMap<Integer, Double>> report = metricCollection.runForFolder(reportedDir);
//                        if (writeTimeSeries) {
//                            printTimeSeries(sortedMetrics, report, reportedDir, outputFileName);
//                        }
//                        dropWarmUpEntriesAndLast(report, warmupEntries);
//                        Map<Metric, List<Double>> hhhMetricMeanVar = computeMeanVar(report);
//                        printMetrics(sortedMetrics, hhhMetricMeanVar, pw);
//                    } catch (IOException e) {
//                        System.err.println(e.getMessage());
//                    }
//                    metricCollection.reset();
//                }
//            }
//        }
//    }

    private void dropWarmUpEntriesAndLast(Map<Metric, SortedMap<Integer, Double>> report, int warmupEntries) {
        for (Map.Entry<Metric, SortedMap<Integer, Double>> entry : report.entrySet()) {
            SortedMap<Integer, Double> value = entry.getValue();
            if (value.size() > 0) {
                value.remove(value.lastKey());
            }
            for (int i = 0; i < warmupEntries && value.size() > 0; i++) {
                value.remove(value.firstKey());
            }
        }
    }

    private void printTimeSeries(List<Metric> sortedMetrics, Map<Metric, SortedMap<Integer, Double>> report,
                                 File reportedDir, String outputFileName) throws IOException {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(reportedDir.getAbsolutePath() + "/" + outputFileName)))) {
            for (Metric metric : sortedMetrics) {
                pw.print("," + metric);
            }
            pw.println();
            Set<Integer> steps = null;
            steps = report.get(sortedMetrics.get(0)).keySet();
            for (Integer step : steps) {
                pw.print(step);
                for (Metric sortedMetric : sortedMetrics) {
                    Double aDouble = report.get(sortedMetric).get(step);
                    pw.print("," + (aDouble == null ? 0 : aDouble));
                }
                pw.println();
            }
        }
    }

    protected void printMetrics(List<Metric> sortedMetrics, Map<Metric, List<Double>> report, PrintWriter bw) {
        for (Metric metric : sortedMetrics) {
            for (Double aDouble : report.get(metric)) {
                bw.print(", " + aDouble);
            }
        }
        bw.println();
    }

    private List<Metric> getSortedMetrics(Map<Metric, List<Double>> report) {
        List<Metric> metrics = new ArrayList<>();
        for (Metric hhhMetric : report.keySet()) {
            metrics.add(hhhMetric);
        }
        Collections.sort(metrics, MetricCollection.comparator);
        return metrics;
    }

    /**
     * @param values
     * @return a map of list with two elements (mean,var) for each metric
     */
    protected Map<Metric, List<Double>> computeMeanVar(Map<Metric, SortedMap<Integer, Double>> values) {
        Map<Metric, List<Double>> output = new HashMap<>();
        for (Map.Entry<Metric, SortedMap<Integer, Double>> entry : values.entrySet()) {
            List<Double> meanVar = new ArrayList<>(2);
            output.put(entry.getKey(), meanVar);
            meanVar.add(getMean(entry.getValue().values()));
            meanVar.add(getVar(entry.getValue().values(), meanVar.get(0)));
        }
        return output;
    }

    private class MapCallable implements Callable<MetricsReport>, Runnable {
        private final MetricCollection metricCollection;
        private final File reportedDir;
        private final List<Metric> sortedMetrics;
        private ComputeMetricsScript.MetricsReport metricsReport;

        public MapCallable(MetricCollection metricCollection, File reportedDir, List<Metric> sortedMetrics) {
            this.metricCollection = metricCollection;
            this.reportedDir = reportedDir;
            this.sortedMetrics = sortedMetrics;
        }

        @Override
        public MetricsReport call() throws Exception {
            Map<Metric, SortedMap<Integer, Double>> report = metricCollection.runForFolder(reportedDir);
            if (writeTimeSeries) {
                printTimeSeries(sortedMetrics, report, reportedDir, outputFileName);
            }
            metricsReport = new MetricsReport(report, reportedDir);
            return metricsReport;
        }

        public MetricsReport getMetricsReport() {
            return metricsReport;
        }

        @Override
        public void run() {
            try {
                call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MetricsReport {
        private Map<Metric, SortedMap<Integer, Double>> report;
        private File reportedDir;

        private MetricsReport(Map<Metric, SortedMap<Integer, Double>> report, File reportedDir) {
            this.report = report;
            this.reportedDir = reportedDir;
        }

        private Map<Metric, SortedMap<Integer, Double>> getReport() {
            return report;
        }

        private File getReportedDir() {
            return reportedDir;
        }
    }
}
