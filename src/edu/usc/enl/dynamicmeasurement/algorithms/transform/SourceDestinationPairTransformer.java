package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 5/8/2014
 * Time: 9:43 AM
 */
public class SourceDestinationPairTransformer extends TrafficTransformer {
    private Map<Long, Map<Long, Long>> keyItemMap = new HashMap<>();

    public SourceDestinationPairTransformer(Element element) {
        super(element);
    }

    public SourceDestinationPairTransformer(WildcardPattern filter) {
        super(filter);
    }

    public SourceDestinationPairTransformer(PacketUser nextUser, WildcardPattern filter) {
        super(nextUser, filter);
    }

    @Override
    public void reset() {
        super.reset();
        keyItemMap.clear();
    }

//    public void print() {
//        DataPacket p = new DataPacket(getStep(), 0, 0, 0, 0, 0, 9);
//        for (Map.Entry<Long, Map<Long, Long>> entry : keyItemMap.entrySet()) {
//            p.setSrcIP(entry.getKey());
//            for (Map.Entry<Long, Long> item : entry.getValue().entrySet()) {
//                p.setDstIP(item.getKey()); //////////////////////////////////////////////////
//                p.setSize(item.getValue());
//                pw.println(p.print());
//            }
//        }
//    }

    @Override
    protected void process2(DataPacket p) {
        Map<Long, Long> items = keyItemMap.get(p.getSrcIP());
        if (items == null) {
            items = new HashMap<>();
            keyItemMap.put(p.getSrcIP(), items);
        }
        Long size = items.get(p.getDstIP());
        if (size == null) {
            size = 0l;
        }
        size += (long) p.getSize();
        items.put(p.getDstIP(), size);
    }

    @Override
    protected void step(EpochPacket ep) {
        for (Map.Entry<Long, Map<Long, Long>> entry : keyItemMap.entrySet()) {
            for (Map.Entry<Long, Long> item : entry.getValue().entrySet()) {
                DataPacket p = new DataPacket(ep.getTime(), entry.getKey(), item.getKey(), 0, 0, 0, item.getValue());
                passPacket(p);
            }
        }
        passPacket(ep);
    }
}
