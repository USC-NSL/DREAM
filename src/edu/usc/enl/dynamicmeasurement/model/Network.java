package edu.usc.enl.dynamicmeasurement.model;

import edu.usc.enl.dynamicmeasurement.algorithms.tasks.hhh.flow.multiswitch.MatrixSet;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.MonitorPoint;
import edu.usc.enl.dynamicmeasurement.model.monitorpoint.WildcardMonitorPoint;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 5:14 PM <br/>
 * This is a wrapper class over moinitor points to parse their configuration.
 */
public class Network {
    private Set<MonitorPoint> monitorPoints;

    public Network(Element element) {
        MatrixSet.MatrixMapping<MonitorPoint> mapping = new MatrixSet.MatrixMapping<>();
        Map<String, Element> switches = Util.getChildrenProperties(element, "Switch");
        for (Map.Entry<String, Element> entry : switches.entrySet()) {
            Element switchElement = entry.getValue();
            NodeList prefixesNodes = switchElement.getElementsByTagName("Prefix");
            Set<WildcardPattern> prefixes = new HashSet<>();
            for (int i = 0; i < prefixesNodes.getLength(); i++) {
                Node item = prefixesNodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    prefixes.add(new WildcardPattern(((Element) item).getAttribute(ConfigReader.PROPERTY_VALUE), 0));
                }
            }
            int capacity = Integer.parseInt(switchElement.getAttribute("capacity"));
            WildcardMonitorPoint monitorPoint = new WildcardMonitorPoint(capacity, prefixes);
            monitorPoint.setIntId(Integer.parseInt(switchElement.getAttribute(ConfigReader.PROPERTY_NAME)));
            if (switchElement.hasAttribute("id")) {
                monitorPoint.setStringId(switchElement.getAttribute("id"));
            }
            //create monitor points using the set of subregions
            mapping.add(monitorPoint);
        }
        monitorPoints = new MatrixSet<>(mapping);
        monitorPoints.addAll(mapping);
    }

    public Set<MonitorPoint> getMonitorPoints() {
        return monitorPoints;
    }

    public MonitorPoint getFirstMonitorPoints() {
        return monitorPoints.iterator().next();
    }
}
