package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 7/26/13
 * Time: 5:04 PM
 */
public class RandomMappingPacketUser extends TrafficTransformer {
    private final Map<Long, Double> ipTraffic = new HashMap<>();
    private final Map<Long, Long> ipMapping = new HashMap<>();
    private double changeFraction;
    private int changeStepNum;
    private Random random;

    public RandomMappingPacketUser(Element e) {
        super(e);
        Map<String, Element> childrenProperties = Util.getChildrenProperties(e, "Property");
        this.random = new Random(Long.parseLong(childrenProperties.get("Random").getAttribute(ConfigReader.PROPERTY_VALUE)));
        this.changeFraction = Double.parseDouble(childrenProperties.get("ChangeFraction").getAttribute(ConfigReader.PROPERTY_VALUE));
        this.changeStepNum = Integer.parseInt(childrenProperties.get("ChangeStepNum").getAttribute(ConfigReader.PROPERTY_VALUE));
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
        int step = p.getStep();
        if (step > 0 && step % changeStepNum == 0) {
            remap();
        }

        for (Map.Entry<Long, Double> entry : ipTraffic.entrySet()) {
            Long srcIP = entry.getKey();
            Long newSrcIP = ipMapping.get(srcIP);
            if (newSrcIP == null) {
                newSrcIP = srcIP;
                ipMapping.put(srcIP, newSrcIP);
            }
            edu.usc.enl.dynamicmeasurement.data.DataPacket p2 = new edu.usc.enl.dynamicmeasurement.data.DataPacket(time, 0, 0, 0, 0, 0, 0);
            p2.setSrcIP(newSrcIP);
            p2.setSize(entry.getValue());
            passPacket(p2);
        }
        passPacket(p);
    }

    private void remap() {
        List<Long> IPs = new ArrayList<>(ipMapping.keySet());
        Collections.shuffle(IPs, random);
        double toChangeNum = ipMapping.size() * changeFraction;
        for (int i = 0; i < toChangeNum; i++) {
            Long first = IPs.get(i++);
            Long second = IPs.get(i);
            Long firstValue = ipMapping.get(first);
            ipMapping.put(first, ipMapping.get(second));
            ipMapping.put(second, firstValue);
        }
    }

    @Override
    protected void reset() {
        super.reset();
        ipTraffic.clear();
    }
}
