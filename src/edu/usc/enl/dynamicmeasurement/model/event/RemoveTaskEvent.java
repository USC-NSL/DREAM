package edu.usc.enl.dynamicmeasurement.model.event;

import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 10:23 AM
 */
public class RemoveTaskEvent extends TaskEvent {

    public RemoveTaskEvent(Element element) {
        super(element);
    }

    @Override
    public void run() {
        Element property = (Element) element.getElementsByTagName("Property").item(0);
        if (property.getAttribute(ConfigReader.PROPERTY_NAME).equals(REMOVE_EVENT_NAME_ATT)) {
            handler.removeTask(property.getAttribute(ConfigReader.PROPERTY_VALUE), getEpoch());
        }
    }
}