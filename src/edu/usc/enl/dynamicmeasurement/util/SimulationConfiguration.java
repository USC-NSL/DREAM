package edu.usc.enl.dynamicmeasurement.util;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/23/13
 * Time: 8:43 PM <br/>
 * Simulation configurations that does not fit anywhere else!
 */
public class SimulationConfiguration {
    private Map<String, String> properties;
    public static int threadsNum = Runtime.getRuntime().availableProcessors();

    public SimulationConfiguration(Element element) {
        properties = new HashMap<>();
        Map<String, Element> properties1 = Util.getChildrenProperties(element, "Property");
        for (Map.Entry<String, Element> entry : properties1.entrySet()) {
            properties.put(entry.getKey(), entry.getValue().getAttribute(ConfigReader.PROPERTY_VALUE));
        }
    }

    public long getEpoch() {
        return Long.parseLong(properties.get("Epoch"));
    }

    public long getStartTime() {
        return Long.parseLong(properties.get("StartTime"));
    }

    public String get(String key) {
        return properties.get(key);
    }

    public int getThreads() {
        return threadsNum;
    }
}
