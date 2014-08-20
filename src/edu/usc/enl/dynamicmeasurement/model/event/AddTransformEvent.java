package edu.usc.enl.dynamicmeasurement.model.event;

import edu.usc.enl.dynamicmeasurement.algorithms.transform.TrafficTransformer;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 10:23 AM  <br/>
 * Registers a transform in the transform handler
 */
public class AddTransformEvent extends TransformEvent {

    public AddTransformEvent(Element transformElement) {
        super(transformElement);
    }


    @Override
    public void run() throws Exception {
        Element e = Util.getChildrenProperties2(element, "Property").iterator().next();
        TrafficTransformer transform = (TrafficTransformer) Class.forName(e.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(e);
        transform.setName2(e.getAttribute(ConfigReader.PROPERTY_NAME));
        handler.addTransform(transform);
    }
}
