package edu.usc.enl.dynamicmeasurement.metric.metriccollection;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.metric.Metric;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.util.NumberAwareComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/27/13
 * Time: 9:52 PM
 */
public abstract class MetricCollection implements Cloneable {
    public static final NumberAwareComparator comparator = new NumberAwareComparator();

    public abstract Map<Metric, SortedMap<Integer, Double>> runForFolder(File f) throws IOException;

    public abstract List<Metric> getMetrics();

    @Deprecated
    public void reset() {
        List<Metric> metrics1 = getMetrics();
        for (Metric metric : metrics1) {
            metric.reset();
        }
    }

    public abstract MetricCollection clone();

    public abstract void init(File parentFolder) throws MetricCollectionInitializationException;

    protected MetricTask getTask(String folder, MetricTask task) {
        try {
            task.loadLog(folder);

            File file = new File(folder + "/config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            Element rootElement = doc.getDocumentElement();
            rootElement.normalize();
            NodeList properties = rootElement.getElementsByTagName("Property");
            for (int i = 0; i < properties.getLength(); i++) {
                Node item = properties.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    task.initConfig(item);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return task;
    }

    protected Map<Integer, List<WildcardPattern>> LoadWildcardPatterns(String file) throws IOException {
        SortedMap<Integer, List<WildcardPattern>> output = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int lineNo = 1;
            int lastTime = -1;
            List<WildcardPattern> hhh = null;
            while (br.ready()) {
                String line = br.readLine();
                StringTokenizer st = new StringTokenizer(line, ",");
                try {
                    int s = Integer.parseInt(st.nextToken());
                    if (lastTime != s) {
                        lastTime = s;
                        hhh = new LinkedList<>();
                        output.put(lastTime, hhh);
                    }
                    WildcardPattern w = new WildcardPattern(st.nextToken(), Double.parseDouble(st.nextToken()));
                    if (hhh.contains(w)) {
                        System.err.println("Duplicate hhh " + w + " at time " + lastTime+" in "+file+" line: "+line);
                        continue;
                    }
                    hhh.add(w);
                } catch (Exception e) {
                    throw new RuntimeException("Incorrect file format in line " + lineNo + " of " + file, e);
                }
                lineNo++;
            }
        }
        return output;
    }

    public static class MetricTask {
        private int start;
        private int finish;
        private WildcardPattern filter;
        private int drop = -1;

        public int getStart() {
            return start;
        }

        public int getFinish() {
            return finish;
        }

        public WildcardPattern getFilter() {
            return filter;
        }

        public void loadLog(String folder) throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(folder + "/task.log"))) {
                while (br.ready()) {
                    String line = br.readLine();
                    String[] split = line.split(",");
                    switch (split[0]) {
                        case "Start":
                            start = Integer.parseInt(split[1]);
                            break;
                        case "Finish":
                            finish = Integer.parseInt(split[1]);
                            break;
                        case "Drop":
                            drop = Integer.parseInt(split[1]);
                            break;
                    }
                }
            }
        }

        public void initConfig(Node item) {
            switch (((Element) item).getAttribute(ConfigReader.PROPERTY_NAME)) {
                case "Filter":
                    filter = new WildcardPattern(((Element) item).getAttribute("value"), 0);
                    break;
//                case "Start":
//                    start = Integer.parseInt(((Element) item).getAttribute("value"));
//                    break;
//                case "Finish":
//                    finish = Integer.parseInt(((Element) item).getAttribute("value"));
//                    break;
            }
        }

        public int getDrop() {
            return drop;
        }

        public void setDrop(int drop) {
            this.drop = drop;
        }
    }

    public static class ThresholdMetricTask extends MetricTask {
        double threshold;

        public double getThreshold() {
            return threshold;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public void initConfig(Node item) {
            super.initConfig(item);
            switch (((Element) item).getAttribute(ConfigReader.PROPERTY_NAME)) {
                case "Threshold":
                    threshold = Double.parseDouble(((Element) item).getAttribute("value"));
                    break;
            }
        }
    }
}
