package edu.usc.enl.dynamicmeasurement.algorithms.transform;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.model.Packet;
import edu.usc.enl.dynamicmeasurement.model.WildcardPattern;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 10:39 AM <br/>
 * A special pipe for transforming the traffic
 */
public abstract class TrafficTransformer extends PacketPipe {
    protected WildcardPattern filter;
    private Packet lastPacket;
    private String name2;

    public TrafficTransformer(Element element) {
        super();
        Map<String, Element> childrenProperties = Util.getChildrenProperties(element, "Property");
        filter = new WildcardPattern(childrenProperties.get("Filter").getAttribute(ConfigReader.PROPERTY_VALUE), 0);
    }

    protected TrafficTransformer(WildcardPattern filter) {
        this.filter = filter;
    }

    public TrafficTransformer(PacketUser nextUser, WildcardPattern filter) {
        super(nextUser);
        this.filter = filter;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public WildcardPattern getFilter() {
        return filter;
    }


    public boolean match(DataPacket p) {
        return filter.match(p.getSrcIP());
    }

    @Override
    protected void passPacket(Packet p) {
        lastPacket = p;
        super.passPacket(p);
    }

    public boolean sent(Packet p) {
        return p == lastPacket;
    }

}
