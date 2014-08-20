package edu.usc.enl.dynamicmeasurement.model.event;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.trace.FilterTraceMapping;
import edu.usc.enl.dynamicmeasurement.data.trace.InputTrace;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/22/13
 * Time: 8:36 PM  <br/>
 * Registers a prefix in the handler
 */
public class AddTrafficEvent extends Event {
    private FilterTraceMapping handler;

    public AddTrafficEvent(Element element) {
        super(element);
    }

    public void setHandler(FilterTraceMapping handler) {
        this.handler = handler;
    }

    @Override
    public void run() throws Exception {
        Element e = Util.getChildrenProperties(element, "Property").values().iterator().next();
        InputTrace inputTrace = (InputTrace) Class.forName(e.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(e);
        handler.addTraffic(inputTrace, getEpoch());
    }

}
