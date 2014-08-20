package edu.usc.enl.dynamicmeasurement.model.event;


import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 10:23 AM<br/>
 * Add a task into the system.
 */
public class AddTaskEvent extends TaskEvent {

    public AddTaskEvent(Element element) {
        super(element);
    }

    @Override
    public void run() throws Exception {
        Element e = Util.getChildrenProperties(element, "Property").values().iterator().next();

        Task2 task = (Task2) Class.forName(e.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(e);
        handler.addTask(task, getEpoch());
    }
}
