package edu.usc.enl.dynamicmeasurement.model.event;

import org.w3c.dom.Element;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/1/13
 * Time: 10:40 PM <br/>
 * The abstract class of the events in the system.
 * <p>The XML constructor requires the time attribute and the value attribute.
 * The value attribute must refer to the class of the event.
 * Internal Property tags will be used to apply the event. </p>
 */
public abstract class Event {
    /**
     * From the configuration file
     */
    protected final Element element;
    /**
     * The epoch that the event must happen
     */
    private int epoch;
    public static final String REMOVE_EVENT_NAME_ATT = "Id";

    public Event(Element element) {
        this.epoch = Integer.parseInt(element.getAttribute("time"));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    public abstract void run() throws Exception;

    public int getEpoch() {
        return epoch;
    }

    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "," + epoch;
    }

    public static class EventTimeComparator implements Comparator<Event> {

        @Override
        public int compare(Event o1, Event o2) {
            return o1.epoch - o2.epoch;
        }
    }

}
