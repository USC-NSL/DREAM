package edu.usc.enl.dynamicmeasurement.util;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MatrixSet;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.Network;
import edu.usc.enl.dynamicmeasurement.util.multithread.MultiThread;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 3/17/13
 * Time: 9:38 PM
 */
public class Util {
    public static Random random;
    private static String rootFolder;
    private static Network network;
    private static SimulationConfiguration simulationConfiguration;
    private static MultiThread multiThread;
    private static Map<String, ControlledBufferWriter> writers = new HashMap<>();

    public static SimulationConfiguration getSimulationConfiguration() {
        return simulationConfiguration;
    }

    public static void setSimulationConfiguration(SimulationConfiguration simulationConfiguration) {
        Util.simulationConfiguration = simulationConfiguration;
    }

    public static void setRandom(int randomSeedIndex) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("random.txt"));
            for (int i = 0; i < randomSeedIndex; i++) {
                br.readLine();
            }
            random = new Random(Long.parseLong(br.readLine()));
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <E> Set<E> cloneSet(Set<E> a) {
        Set<E> output = null;
        if (a instanceof MatrixSet) {
            output = (Set<E>) ((MatrixSet) a).clone();
        } else {
            output = (Set<E>) ((HashSet<E>) a).clone();
        }
        return output;
    }

    public static Map<String, Element> getChildrenProperties(Element e, String tagName) {
        Map<String, Element> output = new HashMap<>();
        NodeList childNodes = e.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals(tagName)) {
                output.put(((Element) item).getAttribute(ConfigReader.PROPERTY_NAME), (Element) item);
            }
        }
        return output;
    }

    public static List<Element> getChildrenProperties2(Element e, String tagName) {
        List<Element> output = new LinkedList<>();
        NodeList childNodes = e.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE && item.getNodeName().equals(tagName)) {
                output.add((Element) item);
            }
        }
        return output;
    }

    public static String getRootFolder() {
        if (rootFolder == null) {
            setRootFolder(".");
        }

        return rootFolder;
    }

    public static void setRootFolder(String rootFolder) {
        new File(rootFolder).mkdirs();
        Util.rootFolder = rootFolder;
    }

    public static Network getNetwork() {
        return network;
    }

    public static void setNetwork(Network network) {
        Util.network = network;
    }

    public static void flushAllControlledWriters() {
        for (Iterator<Map.Entry<String, ControlledBufferWriter>> iterator = writers.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, ControlledBufferWriter> next = iterator.next();
//            System.out.println(next.getKey());
            if (next.getValue().isClose()) {
                iterator.remove();
            } else {
                next.getValue().flush2();
            }
        }
    }

    public static ControlledBufferWriter getNewWriter(String fileName, boolean dummy) throws FileNotFoundException {
        String key = new File(fileName).getAbsolutePath();
        ControlledBufferWriter controlledBufferWriter = new ControlledBufferWriter(fileName, dummy);
        writers.put(key, controlledBufferWriter);
        return controlledBufferWriter;
    }

    public static ControlledBufferWriter getNewWriter(String fileName) throws FileNotFoundException {
        if (simulationConfiguration == null) {
            throw new NullPointerException("got writer before loading configuration");
        }
        String noFile = simulationConfiguration.get("NoFile");
        boolean dummy = Boolean.valueOf(noFile);
        return getNewWriter(fileName, dummy);
    }
}
