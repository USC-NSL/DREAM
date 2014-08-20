package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/29/13
 * Time: 1:21 PM  <br/>
 * Does not pass the traffic that does not match its filter.
 */
public class PacketFilter extends TrafficTransformer {

    public PacketFilter(Element element) {
        super(element);
    }

    public PacketFilter(PacketUser nextUser, WildcardPattern filter) {
        super(nextUser, filter);
    }

    @Override
    protected void process2(edu.usc.enl.dynamicmeasurement.data.DataPacket p) {
        if (match(p)) {
            passPacket(p);
        }
    }

    protected void step(EpochPacket p) {
        passPacket(p);
    }

}
