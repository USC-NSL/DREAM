package edu.usc.enl.dynamicmeasurement.model.event;

import edu.usc.enl.dynamicmeasurement.algorithms.transform.TransformHandlerInterface;
import org.w3c.dom.Element;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/2/13
 * Time: 11:15 AM
 */
public abstract class TransformEvent extends Event {
    protected TransformHandlerInterface handler;


    public TransformEvent(Element element) {
        super(element);
    }

    public void setHandler(TransformHandlerInterface handler) {
        this.handler = handler;
    }
}
