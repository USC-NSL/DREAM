package edu.usc.enl.dynamicmeasurement.process;

import edu.usc.enl.dynamicmeasurement.model.Packet;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 9/5/13
 * Time: 7:23 PM <br/>
 * This packet signals an end of a measurement epoch to all components
 */
public class EpochPacket extends Packet {
    /**
     * The current epoch >=0
     */
    private final int step;

    public EpochPacket(long time, int step) {
        super(time);
        this.step = step;
    }

    public int getStep() {
        return step;
    }
}
