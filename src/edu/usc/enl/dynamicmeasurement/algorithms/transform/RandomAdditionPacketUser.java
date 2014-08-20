package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/27/13
 * Time: 5:23 PM <br/>
 *
 */
public class RandomAdditionPacketUser extends TrafficTransformer {
    private final Map<Long, Double> ipTraffic = new HashMap<>();
    private double changeFraction;
    private Random random;

    public RandomAdditionPacketUser(Element element) {
        super(element);
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        this.changeFraction = Double.parseDouble(childrenProperties.get("ChangeFraction").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.random = new Random(Long.parseLong(childrenProperties.get("Random").getAttribute(ConfigReader.PROPERTY_VALUE)));
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
        double sum = 0;
        for (Double traffic : ipTraffic.values()) {
            sum += traffic;
        }
        double toChangeMax = sum / ipTraffic.size() * changeFraction * 2;
        double sum2 = 0;
        for (Map.Entry<Long, Double> entry : ipTraffic.entrySet()) {
            Long srcIP = entry.getKey();
            edu.usc.enl.dynamicmeasurement.data.DataPacket p2 = new edu.usc.enl.dynamicmeasurement.data.DataPacket(time, 0, 0, 0, 0, 0, 0);
            p2.setSrcIP(srcIP);
            double size = entry.getValue() * (1 - changeFraction) + random.nextDouble() * toChangeMax;
            p2.setSize(size);
            sum2 += size;
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
