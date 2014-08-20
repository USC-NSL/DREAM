package edu.usc.enl.dynamicmeasurement.model.event;


import edu.usc.enl.dynamicmeasurement.algorithms.tasks.Task2;
import edu.usc.enl.dynamicmeasurement.data.ConfigReader;
import edu.usc.enl.dynamicmeasurement.data.trace.FilterTraceMapping;
import edu.usc.enl.dynamicmeasurement.util.Util;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 10:23 AM <br/>
 * Adds a task that reads traces
 */
public class AddTraceTaskEvent extends TaskEvent {
    protected FilterTraceMapping mapping;

    public AddTraceTaskEvent(Element element) {
        super(element);
    }

    public void setMapping(FilterTraceMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void run() throws Exception {
        Task2 task = getTask();
        task.setTraceReader(mapping.getInputTrace(task.getFilter()).getTaskTraceReader(task.getFilter(), getEpoch(), false));
        handler.addTask(task, getEpoch());
    }

    protected Task2 getTask() throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        Element e = Util.getChildrenProperties(element, "Property").values().iterator().next();
        return (Task2) Class.forName(e.getAttribute(ConfigReader.PROPERTY_VALUE)).getConstructor(Element.class).newInstance(e);
    }
}
