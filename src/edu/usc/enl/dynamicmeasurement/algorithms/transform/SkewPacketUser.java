package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/25/13
 * Time: 1:43 PM
 */
public class SkewPacketUser extends TrafficTransformer {
    private Map<Long, Double> ipTraffic = new HashMap<>();
    private double skewChangeFactor = -1;

    public SkewPacketUser(Element element) {
        super(element);
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        this.skewChangeFactor = Double.parseDouble(childrenProperties.get("Skew").getAttribute(ConfigReader.PROPERTY_VALUE));
    }

    public SkewPacketUser(WildcardPattern filter, double skewChangeFactor) {
        super(filter);
        this.skewChangeFactor = skewChangeFactor;
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
        double oldSum = 0;
        double newSum = 0;

        for (Map.Entry<Long, Double> entry : ipTraffic.entrySet()) {
            oldSum += entry.getValue();
            double newSize = Math.pow(entry.getValue(), skewChangeFactor);
            newSum += newSize;
            entry.setValue(newSize);
        }
        double sumScaleFactor =
                //1;
                oldSum / newSum;
        for (Map.Entry<Long, Double> entry : ipTraffic.entrySet()) {
            edu.usc.enl.dynamicmeasurement.data.DataPacket p2 = new edu.usc.enl.dynamicmeasurement.data.DataPacket(time, 0, 0, 0, 0, 0, 0);
            p2.setSrcIP(entry.getKey());
            p2.setSize(entry.getValue() * sumScaleFactor);
            passPacket(p2);
        }
        passPacket(p);
    }

    @Override
    protected void reset() {
        super.reset();
        ipTraffic.clear();
    }
}
