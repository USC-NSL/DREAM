package edu.usc.enl.dynamicmeasurement.data.scenario;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.NeedInitHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.FlowHHHAlgorithm;
import edu.usc.enl.dynamicmeasurement.algorithms.transform.EpochPacer;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.process.Simulator;
import edu.usc.enl.dynamicmeasurement.process.scripts.SumReportPrefixes;
import edu.usc.enl.dynamicmeasurement.util.NumberAwareComparator;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.apache.commons.cli.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/22/13
 * Time: 6:37 PM <br/>
 * Just to generate configuration for the set of events that add tasks and remove them from system
 */
public class MultiFileScenarioGenerator {
    public static final int TRACE_DURATION = 300;
    public static final int TRACE_PREFIX_NUM = 16;
    public static final int STEPS_DURATION = 1;
    public static final Random RANDOM = new Random(234927498274l);
    protected static int folderPerTrace = 1;
    protected final Document doc;
    protected final Element rootNode;
    private final Random traceShuffleRandom = new Random(5883342209986834464l);
    private final Random taskTypeSelectionRandom;
    public List<Trace> traces;
    int traceID = 0;
    private WildcardPattern nextTraceWildcardPattern = null;
    private Iterator<CacheKey> traceFolderIterator;
    private Element readTracePrototypeElement;
    private List<CacheKey> traceFiles;
    private Map<CacheKey, List<WildcardPattern>> cache = new HashMap<>();
    private int maxTaskNum;
    private final List<Element> addTaskPrototypeElements;
    private Element removeTaskPrototypeElement;

    public MultiFileScenarioGenerator(List<Element> addTaskPrototypeElements, Element removeTaskPrototypeElement,
                                      Element otherElementsRoot, Element readTracePrototypeElement,
                                      List<CacheKey> traceFiles, int maxTaskNum) throws ParserConfigurationException {
        this.removeTaskPrototypeElement = removeTaskPrototypeElement;
        this.addTaskPrototypeElements = addTaskPrototypeElements;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        rootNode = (Element) doc.importNode(otherElementsRoot, true);
        doc.appendChild(rootNode);
        this.readTracePrototypeElement = readTracePrototypeElement;
        traces = new ArrayList<>();
        this.traceFiles = traceFiles;
        this.maxTaskNum = maxTaskNum;
        taskTypeSelectionRandom = new Random(8320112876340370721l);
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        //read element prototypes
        //String addTaskPrototypeFile = "resource/scenario/addEventTemplate.xml";
        String removeTaskPrototypeFile = "resource/scenario/removeEventTemplate.xml";
        String otherElementsPrototypeFile = "resource/scenario/emptyEventTemplate.xml";
        String readTraceFile = "resource/scenario/readTraceTemplate.xml";

        int maxTaskNum = 1;
        double taskArrivalRate = 1;
        double taskDurationMean = 300;
        String outputFileName = "event.xml";
        String tracesRootFolder = "../trace";
        String addTaskPrototypeFiles = "";
        {
            Options options = new Options();
            options.addOption(new Option("h", false, "Shows this help"));
            options.addOption(OptionBuilder.withArgName("filename").hasArg().isRequired().withDescription("Output file").create('o'));
            options.addOption(OptionBuilder.withArgName("foldername").hasArg().isRequired().withDescription("Trace folder").create('t'));
            options.addOption(OptionBuilder.withArgName("integer").isRequired().withType(Number.class).hasArg().withDescription("Number of tasks").create('n'));
            options.addOption(OptionBuilder.withArgName("double").isRequired().withType(Number.class).hasArg().withDescription("Task arrival rate").create('r'));
            options.addOption(OptionBuilder.withArgName("integer").isRequired().withType(Number.class).hasArg().withDescription("Task duration").create('d'));
            options.addOption(OptionBuilder.withArgName("List of templates").isRequired().hasArg().withDescription("Task configuration templates").create("tasks"));

            CommandLineParser parser = new PosixParser();
            try {
                CommandLine cmd = parser.parse(options, args);
                if (cmd.hasOption("h") || cmd.getOptions().length < options.getRequiredOptions().size()) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp("java <classname>", options, true);
                    System.exit(0);
                }
                outputFileName = cmd.getOptionValue('o');
                taskArrivalRate = Double.parseDouble(cmd.getOptionValue('r'));
                taskDurationMean = Integer.parseInt(cmd.getOptionValue('d'));
                maxTaskNum = Integer.parseInt(cmd.getOptionValue('n'));
                tracesRootFolder = cmd.getOptionValue('t');
                addTaskPrototypeFiles = cmd.getOptionValue("tasks");
            } catch (ParseException e) {
                System.err.println("Parsing failed.  Reason: " + e.getMessage());
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java <classname>", options, true);
                System.exit(0);
            }
        }

        List<Element> addTaskPrototypeElements = new ArrayList<>();
        for (String addTaskPrototypeFile : addTaskPrototypeFiles.split(",")) {
            addTaskPrototypeElements.add(ConfigReader.readElement(addTaskPrototypeFile));
        }

        folderPerTrace = (int) Math.ceil(taskDurationMean / 300);
        MultiFileScenarioGenerator generator = new MultiFileScenarioGenerator(
                addTaskPrototypeElements,
                ConfigReader.readElement(removeTaskPrototypeFile), ConfigReader.readElement(otherElementsPrototypeFile),
                ConfigReader.readElement(readTraceFile), listFolders(tracesRootFolder), maxTaskNum);
//        generator.generateByDuration(listFolders(tracesRootFolder), numEpochs, taskArrivalRate, taskDurationMean, maxTaskNum);
        generator.generateByTaskNum(taskArrivalRate, taskDurationMean);
        generator.commit(outputFileName);
    }

    /**
     * Write the configuration to the output file
     *
     * @param fileName
     */
    public void commit(String fileName) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File f = new File(fileName);
            f.getParentFile().mkdirs();
            StreamResult result = new StreamResult(f);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }


    protected static List<CacheKey> listFolders(String tracesRootFolder) {
        List<CacheKey> output;
        File file = new File(tracesRootFolder);
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (files == null) {
            System.err.println("Trace folder " + tracesRootFolder + " cannot be accessed");
            System.exit(1);
        }
        output = new ArrayList<>(files.length);
        Arrays.sort(files, new NumberAwareComparator());
        int filesIndex = 0;
        while (filesIndex < files.length) {
            String[] folders = new String[folderPerTrace];
            for (int i = 0; i < folderPerTrace; i++) {//TODO what if it 12 is not dividable by folderPerTrace
                folders[i] = files[filesIndex].getPath();
                filesIndex++;
            }
            output.add(new CacheKey(folders));
        }
        return output;
    }

    protected void writeTaskArrival(int taskNum, int epoch, WildcardPattern filter) {
        //set task name
        Element addTaskPrototypeElement1 = addTaskPrototypeElements.get(taskTypeSelectionRandom.nextInt(addTaskPrototypeElements.size()));
        Element taskElement = Util.getChildrenProperties2(addTaskPrototypeElement1, "Property").get(0);
        taskElement.setAttribute(ConfigReader.PROPERTY_NAME, taskNum + "");
        //set task filter
        NodeList descendantPropertyNodes = taskElement.getElementsByTagName("Property");
        for (int i = 0; i < descendantPropertyNodes.getLength(); i++) {
            Element item = (Element) descendantPropertyNodes.item(i);
            if (item.getAttribute(ConfigReader.PROPERTY_NAME).equals("Filter")) {
                item.setAttribute(ConfigReader.PROPERTY_VALUE, filter.toStringNoWeight());
            }
//            if (item.getAttribute(ConfigReader.PROPERTY_NAME).equals("Threshold")) {
//                item.setAttribute(ConfigReader.PROPERTY_VALUE, "" + thresholds[thresholdTypeSelectionRandom.nextInt(thresholds.length)]);
//            }
        }

        //set event time
        addTaskPrototypeElement1.setAttribute("time", epoch + "");
        Node node = doc.importNode(addTaskPrototypeElement1, true);
        rootNode.appendChild(node);
    }

    public void generateByTaskNum(double taskArrivalRate, double taskDurationMean) {
        traceFolderIterator = traceFiles.iterator();
        nextTraceWildcardPattern = null;
        Random arrivalRandom = new Random(29387493274l);
        Random durationRandom = new Random(-93287492749l);
        int taskNum = 0;
        int epoch = 0;
        while (taskNum < maxTaskNum) {
            int newTasksNum = getPoisson(taskArrivalRate, arrivalRandom);
            for (int j = 0; j < newTasksNum; j++) {
                taskNum++;
                int duration = (int) taskDurationMean;
                // (int) Math.round(ScenarioGenerator.getExp(1 / taskDurationMean, durationRandom));
//                duration = Math.min(duration, TRACE_DURATION);
                Task task = new Task(epoch, epoch + duration, taskNum);
                addTask2(task);
                task.write();
                if (taskNum >= maxTaskNum) {
                    break;
                }
            }
            epoch++;
        }
    }

    protected void writeTaskDeparture(int taskName, int epoch) {
        Element property = (Element) removeTaskPrototypeElement.getElementsByTagName("Property").item(0);
        if (property.getAttribute(ConfigReader.PROPERTY_NAME).equals("Id")) {
            property.setAttribute(ConfigReader.PROPERTY_VALUE, taskName + "");
        }
        removeTaskPrototypeElement.setAttribute("time", epoch + "");
        Node node = doc.importNode(removeTaskPrototypeElement, true);
        rootNode.appendChild(node);
    }

//    public void generateByDuration(int numEpochs, double taskArrivalRate, double taskDurationMean) {
//        traceFolderIterator = traceFiles.iterator();
//        nextTraceWildcardPattern = new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH - (int) Math.ceil(Math.log(traceFiles.size()) / Math.log(2)), 0);
//        Random arrivalRandom = new Random(29387493274l);
//        Random durationRandom = new Random(-93287492749l);
//        int taskNum = 0;
//        for (int epoch = 0; epoch < numEpochs; epoch++) {
//            int newTasksNum = ScenarioGenerator.getPoisson(taskArrivalRate, arrivalRandom);
//            for (int j = 0; j < newTasksNum; j++) {
//                taskNum++;
//                int duration = (int) Math.round(ScenarioGenerator.getExp(1 / taskDurationMean, durationRandom));
//                duration = Math.min(duration, TRACE_DURATION);
//                Task task = new Task(epoch, epoch + duration, taskNum);
//                addTask2(task);
//                task.write();
//            }
//        }
//    }

    private void addTask2(Task task) {
        boolean foundTrace = false;
        for (Iterator<Trace> iterator = traces.iterator(); iterator.hasNext(); ) {
            Trace trace = iterator.next();
            List<Prefix> prefixes = trace.getPrefixes();
            for (Prefix prefix : prefixes) {
                if (prefix.canAccommodate2(task)) {
                    foundTrace = true;
                    task.setPrefix(prefix);
                    break;
                }
            }
            if (foundTrace) {
                break;
            } else {//the trace should be full
                iterator.remove();
            }
        }
        if (!foundTrace) {
            createNewTraceFor(task);
        }
    }

    private void addTask(Task task) {
        boolean foundTrace = false;
        for (Iterator<Trace> iterator = traces.iterator(); iterator.hasNext(); ) {
            Trace trace = iterator.next();
            if (task.getStart() > trace.getFinish()) {
                //trace is not useful anymore
                iterator.remove();
                continue;
            }
            if (task.getStart() >= trace.getStart() && task.getFinish() <= trace.getFinish()) {
                List<Prefix> prefixes = trace.getPrefixes();
                for (Prefix prefix : prefixes) {
                    if (prefix.canAccommodate(task)) {
                        foundTrace = true;
                        task.setPrefix(prefix);
                        break;
                    }
                }
            }
            if (foundTrace) {
                break;
            }
        }
        if (!foundTrace) {
            createNewTraceFor(task);
        }
    }

    private void createNewTraceFor(Task task) {
        if (nextTraceWildcardPattern == null) {
//            nextTraceWildcardPattern = new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH - (int) Math.ceil(Math.log(1.0 * maxTaskNum / TRACE_PREFIX_NUM) / Math.log(2)), 0);
            nextTraceWildcardPattern = new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH - (12 - ((int) (Math.log(TRACE_PREFIX_NUM) / Math.log(2)))), 0);
        } else {
//            if (nextTraceWildcardPattern.getData() + 1 >= 1 << (WildcardPattern.TOTAL_LENGTH-nextTraceWildcardPattern.getWildcardNum())) {
//                System.out.println("Prefixes for traces finished");
//                commit(outputFileName);
//                System.exit(1);
//            }
            nextTraceWildcardPattern = new WildcardPattern(nextTraceWildcardPattern.getData() + 1, nextTraceWildcardPattern.getWildcardNum(), 0);
        }
        String[] traceFolders = getNextTraceFolder().files;
        Trace trace = new Trace(task.getStart(), task.getStart() + TRACE_DURATION, nextTraceWildcardPattern, TRACE_PREFIX_NUM, traceFolders, traceID++);
        trace.write();

        traces.add(trace);
        if (trace.getPrefixes().size() == 0) {
            System.err.println("Could not find any prefix in this trace file!");
            System.exit(1);
        } else {
            task.setPrefix(trace.getPrefixes().get(0));
        }

    }

    private CacheKey getNextTraceFolder() {
        if (!traceFolderIterator.hasNext()) {
            System.err.println("Restarting traces.");
            Collections.shuffle(traceFiles, RANDOM);
            traceFolderIterator = traceFiles.iterator(); //start from the beginning
        }
        return traceFolderIterator.next();
    }

    protected static class CacheKey {
        private String[] files;

        private CacheKey(String[] files) {
            this.files = files;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(files);
        }

        @Override
        public String toString() {
            return "CacheKey{" +
                    "files=" + Arrays.toString(files) +
                    '}';
        }
    }

    private static class Prefix {
        private WildcardPattern wildcardPattern;
        private TreeMap<Integer, Integer> fullTime;

        private Prefix(WildcardPattern wildcardPattern) {
            this.wildcardPattern = wildcardPattern;
            fullTime = new TreeMap<>();
        }

        @Override
        public String toString() {
            return "Prefix{" +
                    "wildcardPattern=" + wildcardPattern +
                    ", fullTime=" + fullTime +
                    '}';
        }

        private WildcardPattern getWildcardPattern() {
            return wildcardPattern;
        }

        public boolean canAccommodate(Task task) {
            NavigableMap<Integer, Integer> fullAfter = fullTime.tailMap(task.getStart(), true);
            return fullAfter.size() == 0;
        }

        public void addFull(int start, int finish) {
            fullTime.put(finish, start);
        }

        public boolean canAccommodate2(Task task) {
            return fullTime.size() == 0;
        }
    }

    private class Task {
        private final int start;
        private final int finish;
        private final int id;
        private Prefix prefix;

        private Task(int start, int finish, int id) {
            this.start = start;
            this.finish = finish;
            this.id = id;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "start=" + start +
                    ", finish=" + finish +
                    ", prefix=" + prefix +
                    ", id=" + id +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Task task = (Task) o;

            if (id != task.id) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return id;
        }

        private Prefix getPrefix() {
            return prefix;
        }

        private void setPrefix(Prefix prefix) {
            this.prefix = prefix;
            prefix.addFull(start, finish);
        }

        private int getStart() {
            return start;
        }

        private int getFinish() {
            return finish;
        }

        public void write() {
            writeTaskArrival(id, start, getPrefix().getWildcardPattern());
            writeTaskDeparture(id, finish);
        }
    }

    public static LinkedList<WildcardPattern> getWildcardPatterns(WildcardPattern rootWildcardPattern, int nums) {
        final LinkedList<WildcardPattern> taskFilters = new LinkedList<>();

        FlowHHHAlgorithm.initMonitors(nums, new NeedInitHHHAlgorithm() {
            @Override
            public void addMonitor(WildcardPattern wildcardPattern) {
                taskFilters.add(wildcardPattern);
            }

            @Override
            public WildcardPattern pollAMonitor() {
                return taskFilters.pop();
            }
        }, rootWildcardPattern);
        return taskFilters;
    }

    private class Trace {
        private final String[] folders;
        private final int id;
        private int start;
        private int finish;
        private List<Prefix> prefixes;
        private WildcardPattern wildcardPattern;

        private Trace(int start, int finish, WildcardPattern wildcardPattern, int prefixNum, String[] folders, int id) {
            this.start = start;
            this.finish = finish;
            this.wildcardPattern = wildcardPattern;
            prefixes = new LinkedList<>();
            LinkedList<WildcardPattern> wildcardPatterns = getWildcardPatterns(
                    new WildcardPattern(0, WildcardPattern.TOTAL_LENGTH, 0), prefixNum);

            //pick only large traffic prefixes
            pickPrefixes(folders, wildcardPatterns);
            this.folders = folders;
            this.id = id;
        }

        /**
         * Pick only prefixes that have traffic larger than x% of the total traffic
         *
         * @param folders
         * @param wildcardPatterns
         */
        private void pickPrefixes(String[] folders, List<WildcardPattern> wildcardPatterns) {
            CacheKey entry = new CacheKey(folders);
            List<WildcardPattern> wildcardPatterns1 = cache.get(entry);
            if (wildcardPatterns1 != null) {
                wildcardPatterns = wildcardPatterns1;
            } else {
                readFromSummary(folders, wildcardPatterns);
//                readFromFile(folders, wildcardPatterns);
                cache.put(entry, wildcardPatterns);
            }
            double sum = 0;
            for (WildcardPattern pattern : wildcardPatterns) {
                sum += pattern.getWeight();
            }
            long rootData = wildcardPattern.getData() << wildcardPattern.getWildcardNum();
            for (WildcardPattern pattern : wildcardPatterns) {
                if (pattern.getWeight() > sum / 100) {
//                if (pattern.getWeight() > 0) {
                    int wildcardNum = pattern.getWildcardNum() - (WildcardPattern.TOTAL_LENGTH - wildcardPattern.getWildcardNum());
                    long completeData = rootData + (pattern.getData() << wildcardNum);
                    WildcardPattern p = new WildcardPattern(completeData >>> wildcardNum, wildcardNum, 0);
                    prefixes.add(new Prefix(p));
                }
            }
//            Collections.shuffle(prefixes, traceShuffleRandom);
            System.out.println(Arrays.toString(folders) + ": " + prefixes.size());
        }

        private void readFromSummary(String[] folders, List<WildcardPattern> wildcardPatterns) {
            for (WildcardPattern wildcardPattern : wildcardPatterns) {
                wildcardPattern.setWeight(0);
            }
            for (String folder : folders) {
                Iterator<WildcardPattern> itr = wildcardPatterns.iterator();
                try (BufferedReader br = new BufferedReader(new FileReader(folder + "/summary.txt"))) {
                    while (br.ready()) {
                        String[] line = br.readLine().split(",");
                        WildcardPattern wc = new WildcardPattern(line[0], Double.parseDouble(line[1]));
                        WildcardPattern next = itr.next();
                        next.setWeight(next.getWeight() + wc.getWeight());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void readFromFile(String[] folders, List<WildcardPattern> wildcardPatterns) {
            try {
                List<File> files2 = new ArrayList<>();
                for (String folder : folders) {
                    File folderFile = new File(folder);
                    File[] files = folderFile.listFiles();
                    if (files == null) {
                        throw new RuntimeException("Folder " + folder + " not found");
                    }
                    Arrays.sort(files, new NumberAwareComparator());
                    files2.addAll(Arrays.asList(files));
                }
                for (Iterator<File> iterator = files2.iterator(); iterator.hasNext(); ) {
                    File next = iterator.next();
                    if (next.getName().contains("summary")) {
                        iterator.remove();
                    }
                }
                String[] packetsFile = new String[files2.size()];
                int i = 0;
                for (File file : files2) {
                    packetsFile[i++] = file.getAbsolutePath();
                }

//                        SumReportPrefixesWrite user1 = new SumReportPrefixesWrite(wildcardPatterns, "output/traceprofile/"+folders[0].replaceAll(".*[/\\\\]",""));
                SumReportPrefixes user1 = new SumReportPrefixes(wildcardPatterns, false);

                PacketUser user = new EpochPacer(user1, STEPS_DURATION);
                //run simulator
                new Simulator(0).run(packetsFile, user);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Trace trace = (Trace) o;

            if (wildcardPattern != null ? !wildcardPattern.equals(trace.wildcardPattern) : trace.wildcardPattern != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return wildcardPattern != null ? wildcardPattern.hashCode() : 0;
        }

        private List<Prefix> getPrefixes() {
            return prefixes;
        }

        private int getStart() {
            return start;
        }

        private int getFinish() {
            return finish;
        }

        public void write() {
            Element eventElement = (Element) doc.importNode(readTracePrototypeElement, true);
            Element traceElement = Util.getChildrenProperties2(eventElement, "Property").get(0);
            traceElement.setAttribute(ConfigReader.PROPERTY_NAME, "" + id);
            Map<String, Element> properties = Util.getChildrenProperties(traceElement, "Property");
            Element folderElement = null;
            for (Map.Entry<String, Element> entry : properties.entrySet()) {
                if (entry.getKey().equals("Filter")) {
                    entry.getValue().setAttribute(ConfigReader.PROPERTY_VALUE, wildcardPattern.toStringNoWeight());
                }
                if (entry.getKey().equals("Folder")) {
                    folderElement = entry.getValue();
                    entry.getValue().setAttribute(ConfigReader.PROPERTY_VALUE, folders[0].replaceAll("\\\\", "/"));
                    entry.getValue().setAttribute(ConfigReader.PROPERTY_NAME, "Folder0");
                }
            }
            if (folders.length > 1) {
                //need to add more folder properties
                for (int i = 1; i < folders.length; i++) {
                    String folder = folders[i];
                    Element newChild = (Element) doc.importNode(folderElement, true);
                    newChild.setAttribute(ConfigReader.PROPERTY_VALUE, folders[i].replaceAll("\\\\", "/"));
                    newChild.setAttribute(ConfigReader.PROPERTY_NAME, "Folder" + i);
                    traceElement.appendChild(newChild);
                }
            }

            //set event time
            eventElement.setAttribute("time", start + "");

            rootNode.appendChild(eventElement);
        }
    }

    public static int getPoisson(double lambda, Random random) {
        double l = Math.exp(-lambda);
        int k = 0;
        double p = 1;
        do {
            k++;
            double u = random.nextDouble();
            p *= u;
        } while (p > l);
        return k - 1;
    }

    public static double getExp(double lambda, Random random) {
        return -1 / lambda * Math.log(random.nextDouble());
    }
}
