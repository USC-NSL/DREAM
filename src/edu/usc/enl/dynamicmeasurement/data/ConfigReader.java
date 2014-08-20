package edu.usc.enl.dynamicmeasurement.data;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import edu.usc.enl.dynamicmeasurement.model.Network;
import edu.usc.enl.dynamicmeasurement.model.event.Event;
import edu.usc.enl.dynamicmeasurement.util.SimulationConfiguration;
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
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/1/13
 * Time: 9:49 PM  <br/>
 * The utility class that parses a configuration file.
 */
public class ConfigReader {

    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_NAME = "name";
    private LinkedList<Event> events;
    private TaskHandler handler;

    public static void writeElement(Element e, String fileName) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Node node = doc.importNode(e, true);
        doc.appendChild(node);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

    public static Element readElement(String filename) throws IOException, SAXException, ParserConfigurationException {
        File file = new File(filename);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();
        return rootElement;
    }

    public void read(String filename) throws Exception {
        File file = new File(filename);
        Element rootElement = loadFile(file);
        {
            SimulationConfiguration simulationConfiguration = new SimulationConfiguration(rootElement);
            Util.setSimulationConfiguration(simulationConfiguration);
            String output = simulationConfiguration.get("Output");
            if (output != null) {
                Util.setRootFolder(output);
            } else {
                Util.setRootFolder(file.getParent());//same folder as file
                //Util.setRootFolder(file.getAbsolutePath().replaceAll("\\.[^\\.]*$", ""));//same folder name as file
            }
            //copy the config file to output
            String outputFileName = Util.getRootFolder() + "/config.xml";
            if (!new File(outputFileName).getAbsoluteFile().getAbsolutePath().equals(file.getAbsoluteFile().getAbsolutePath())) {
                try (BufferedReader br = new BufferedReader(new FileReader(file)); PrintWriter pw = new PrintWriter(outputFileName)) {
                    while (br.ready()) {
                        pw.println(br.readLine());
                    }
                }
            }
        }
        {
            Element network1 = Util.getChildrenProperties2(rootElement, "Network").get(0);
            Network network;
            if (network1.hasAttribute("path")) {
                String path = network1.getAttribute("path");
                Element rootElement2 = loadFile(new File(file.getParentFile().getAbsolutePath() + "/" + path));
                network = new Network(rootElement2);
            } else {
                network = new Network(network1);
            }
            Util.setNetwork(network);
        }
        {
            //initialize script
            NodeList handlerNode = rootElement.getElementsByTagName("TaskHandler");
            Node item = handlerNode.item(0);
            Element element = (Element) item;
            handler = (TaskHandler) Class.forName(element.getAttribute("class")).getConstructor(Element.class).newInstance(element);
        }
        events = new LinkedList<>();
        {
            {//Import
                NodeList eventsNode = rootElement.getElementsByTagName("ImportEvents");
                for (int i = 0; i < eventsNode.getLength(); i++) {
                    Element element = (Element) eventsNode.item(i);
                    String path = element.getAttribute("path");
                    Element rootElement2 = loadFile(new File(file.getParentFile().getAbsolutePath() + "/" + path));
                    loadEvents(events, rootElement2);
                }
            }

            loadEvents(events, rootElement);
        }
    }

    public static void loadEvents(LinkedList<Event> events, Element rootElement) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        //initialize events
        NodeList eventsNode = rootElement.getElementsByTagName("Event");
        for (int i = 0; i < eventsNode.getLength(); i++) {
            Element element = (Element) eventsNode.item(i);
            Event event = (Event) Class.forName(element.getAttribute("class")).getConstructor(Element.class).newInstance(element);
            events.add(event);
        }
    }

    public static Element loadFile(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();
        return rootElement;
    }

    public LinkedList<Event> getEvents() {
        return events;
    }

    public TaskHandler getTaskHandler() {
        return handler;
    }
}
