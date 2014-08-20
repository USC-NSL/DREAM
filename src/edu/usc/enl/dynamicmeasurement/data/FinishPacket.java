package edu.usc.enl.dynamicmeasurement.data;

import edu.usc.enl.dynamicmeasurement.model.Packet;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/4/13
 * Time: 5:31 PM <br/>
 * Shows the end of experiment
 */
public class FinishPacket extends Packet {
    public FinishPacket(long time) {
        super(time);
    }
}
