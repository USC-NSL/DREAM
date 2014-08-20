package edu.usc.enl.dynamicmeasurement.model.event;

import edu.usc.enl.dynamicmeasurement.algorithms.taskhandler.TaskHandler;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 11:18 AM
 */
public abstract class TaskEvent extends Event {
    /**
     * The class that will handle this event
     */
    protected TaskHandler handler;

    public TaskEvent(Element element) {
        super(element);
    }

    public void setHandler(TaskHandler handler) {
        this.handler = handler;
    }
}
