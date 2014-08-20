package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 11/11/13
 * Time: 10:49 AM <br/>
 * A traffic transformer that scales traffic down.
 * It regenerates traffic at the end of epoch and ignores traffic from items that have very small traffic
 */
public class ExperimentScale extends TrafficTransformer {
    private Map<Long, Double> ipTraffic = new HashMap<>();
    private double scaleFactor = -1;

    public ExperimentScale(Element element) {
        super(element);
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        this.scaleFactor = Double.parseDouble(childrenProperties.get("Scale").getAttribute(ConfigReader.PROPERTY_VALUE));
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        if (match(p)) {
            Double aDouble = ipTraffic.get(p.getSrcIP());
            if (aDouble == null) {
                aDouble = 0d;
            }
            ipTraffic.put(p.getSrcIP(), aDouble + p.getSize());
        } else {
            passPacket(p);
        }
    }

    @Override
    protected void step(EpochPacket p) {
        long time = p.getTime();

        for (Map.Entry<Long, Double> entry : ipTraffic.entrySet()) {
            edu.usc.enl.dynamicmeasurement.data.DataPacket p2 = new edu.usc.enl.dynamicmeasurement.data.DataPacket(time, 0, 0, 0, 0, 0, 0);
            p2.setSrcIP(entry.getKey());
            p2.setSize(entry.getValue() / scaleFactor);
            if (p2.getSize() > 10) {
                passPacket(p2);
            }
        }
        passPacket(p);
    }

    @Override
    protected void reset() {
        super.reset();
        ipTraffic.clear();
    }
}
