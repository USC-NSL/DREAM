package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/16/13
 * Time: 4:29 PM <br/>
 * Generates packets similar to the matching filte ron the target filter.
 * If the target filter is larger than source filter, zeros will be padded to the end.
 * It also aggregates the packets and sends them at the end of the epoch
 */
public class CopyPackets extends TrafficTransformer {
    private WildcardPattern target;
    private Map<Long, Double> ipTraffic = new HashMap<>();

    public CopyPackets(Element element) {
        super(element);
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        target = new WildcardPattern(childrenProperties.get("Target").getAttribute(ConfigReader.PROPERTY_VALUE), 0);
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        if (match(p)) {
            long srcIP = p.getSrcIP();
            put(p, srcIP);

            //map srcIP to a new srcIP
            srcIP -= filter.getData() << filter.getWildcardNum();
            int wDiff = filter.getWildcardNum() - target.getWildcardNum();
            if (wDiff > 0) {
                srcIP >>>= wDiff;
            } else {
                srcIP <<= wDiff;
            }
            srcIP += target.getData() << target.getWildcardNum();
            put(p, srcIP);
        } else {
            passPacket(p);
        }
    }

    private void put(DataPacket p, long srcIP) {
        Double aDouble = ipTraffic.get(srcIP);
        if (aDouble == null) {
            aDouble = 0d;
        }
        ipTraffic.put(srcIP, aDouble + p.getSize());
    }

    @Override
    protected void reset() {
        super.reset();
        ipTraffic.clear();
    }

    @Override
    protected void step(EpochPacket p) {
        long time = p.getTime();
        for (Map.Entry<Long, Double> entry : ipTraffic.entrySet()) {
            DataPacket p2 = new DataPacket(time, 0, 0, 0, 0, 0, 0);
            p2.setSrcIP(entry.getKey());
            p2.setSize(entry.getValue());
            passPacket(p2);
        }
        passPacket(p);
    }
}
