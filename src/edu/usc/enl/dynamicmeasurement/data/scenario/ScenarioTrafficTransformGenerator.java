package edu.usc.enl.dynamicmeasurement.data.scenario;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.model.event.AddTaskEvent;
import edu.usc.enl.dynamicmeasurement.model.event.AddTraceTaskEvent;
import edu.usc.enl.dynamicmeasurement.model.event.Event;
import edu.usc.enl.dynamicmeasurement.util.Util;
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
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 10/7/13
 * Time: 9:17 PM  <br/>
 * To generate a configuration file for a set of traffic transforms
 */
public class ScenarioTrafficTransformGenerator {
    protected final Document doc;
    protected final Element rootNode;
    private final Element addTransformPrototypeElement;

    public ScenarioTrafficTransformGenerator(Element addTransformPrototypeElement, Element otherElementsRoot) throws ParserConfigurationException {
        this.addTransformPrototypeElement = addTransformPrototypeElement;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();
        rootNode = (Element) doc.importNode(otherElementsRoot, true);
        doc.appendChild(rootNode);
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        WildcardPattern.TOTAL_LENGTH = 32;
        String addTaskPrototypeFile = "resource/scenario/addTransformTemplate.xml";
        String otherElementsPrototypeFile = "resource/scenario/emptyEventTemplate.xml";
        String taskInputFile = "output/hwswitch/sim/events.xml";
        new ScenarioTrafficTransformGenerator(ConfigReader.readElement(addTaskPrototypeFile),
                ConfigReader.readElement(otherElementsPrototypeFile)).run(taskInputFile);
    }

    //May produce fewer transforms comparing to events.xml because it may have repetitive task filters
    private void run(String input) {
        //for each task wildcard pattern
        int transformDepthInTask = 0;
        int transformNumPerTask = 1;// must be smaller than 2^transformDepthInTask
        int transFormNum = 0;
        Map<WildcardPattern, Integer> tasksFilters = getTasksFilters(input);
        List<WildcardPattern> patterns = new ArrayList<>(tasksFilters.keySet());
        Collections.sort(patterns);
        for (WildcardPattern pattern : patterns) {
            for (int i = 0; i < transformNumPerTask; i++) {
                WildcardPattern transformPattern = new WildcardPattern((pattern.getData() << transformDepthInTask) + i,
                        pattern.getWildcardNum() - transformDepthInTask, 0);
                addTransform(transformPattern, transFormNum++, tasksFilters.get(pattern));
            }
        }

        commit(new File(input).getAbsoluteFile().getParent() + "/transform.xml");
    }

    private void addTransform(WildcardPattern filter, int transformNum, int time) {
        Element taskElement = Util.getChildrenProperties2(addTransformPrototypeElement, "Property").get(0);
        taskElement.setAttribute(ConfigReader.PROPERTY_NAME, transformNum + "");
        //set task filter
        NodeList descendantPropertyNodes = taskElement.getElementsByTagName("Property");
        for (int i = 0; i < descendantPropertyNodes.getLength(); i++) {
            Element item = (Element) descendantPropertyNodes.item(i);
            if (item.getAttribute(ConfigReader.PROPERTY_NAME).equals("Filter")) {
                item.setAttribute("value", filter.toStringNoWeight());
            }
        }

        //set event time
        addTransformPrototypeElement.setAttribute("time", 0 + "");//0 just because we have repeating tasks!
        Node node = doc.importNode(addTransformPrototypeElement, true);
        rootNode.appendChild(node);
    }

    private void commit(String fileName) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File f = new File(fileName).getAbsoluteFile();
            f.getParentFile().mkdirs();
            StreamResult result = new StreamResult(f);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private Map<WildcardPattern, Integer> getTasksFilters(String file) {
        Map<WildcardPattern, Integer> output = new HashMap<>();
        try {
            Element rootElement2 = ConfigReader.loadFile(new File(file));
            LinkedList<Event> events = new LinkedList<>();
            ConfigReader.loadEvents(events, rootElement2);
            for (Event event : events) {
                if (event instanceof AddTraceTaskEvent || event instanceof AddTaskEvent) {
                    int epoch = event.getEpoch();
                    NodeList descendantPropertyNodes = event.getElement().getElementsByTagName("Property");
                    for (int i = 0; i < descendantPropertyNodes.getLength(); i++) {
                        Element item = (Element) descendantPropertyNodes.item(i);
                        if (item.getAttribute(ConfigReader.PROPERTY_NAME).equals("Filter")) {
                            String pattern = item.getAttribute(ConfigReader.PROPERTY_VALUE);
                            WildcardPattern wildcardPattern = new WildcardPattern(pattern, 0);
                            int l = output.size();
                            output.put(wildcardPattern, epoch);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}
