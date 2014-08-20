package edu.usc.enl.dynamicmeasurement.model;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/4/13
 * Time: 5:07 PM <br/>
 * A packet in the simulator. This is not necessarily a packet from trace, but it can be used for passing
 * essential information among simulator components.
 */
public class Packet {
    protected long time;

    public Packet(long time) {
        this.time = time;
    }

    protected Packet() {

    }

    public long getTime() {
        return time;
    }
}
