package edu.usc.enl.dynamicmeasurement.model.event;

import edu.usc.enl.dynamicmeasurement.algorithms.transform.PacketPipe;
import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.process.EpochPacket;
import edu.usc.enl.dynamicmeasurement.process.PacketUser;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/1/13
 * Time: 10:42 PM <br/>
 * This is the kind of packet pipe that upon receiving epochs it runs an event.
 * It should be possible to have an implementation that does not need to be pipe.
 */
public class EventRunner extends PacketPipe {
    private LinkedList<Event> events;

    public EventRunner(PacketUser nextUser, LinkedList<Event> events) {
        super(nextUser);
        this.events = events;
        Collections.sort(events, new Event.EventTimeComparator());
        runEvents(-1);  //for 0 events
    }

    @Override
    protected void process2(DataPacket p) {
        passPacket(p);
    }

    @Override
    protected void step(EpochPacket p) {
        passPacket(p);//want all middle things do their step (especially transforms), then add or remove
        runEvents(p.getStep());
    }

    private void runEvents(int step) {
        try {
            while (events.size() > 0) {
                Event event = events.peek();
                if (event.getEpoch() > step + 1) {//want to run events at the beginning of each epoch
                    break;
                } else {
                    events.pop();
                    System.out.println("run " + event);
                    event.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isEmpty() {
        return events.size() == 0;
    }
}
