package edu.usc.enl.dynamicmeasurement.process;

import edu.usc.enl.dynamicmeasurement.data.DataPacket;
import edu.usc.enl.dynamicmeasurement.model.Packet;

/**
 * Created with IntelliJ IDEA.
 * User: Masoud
 * Date: 1/31/13
 * Time: 7:21 PM <br/>
 * A packet user that runs the step method upon receiving a packet of type EpochPacket.
 *
 * @see edu.usc.enl.dynamicmeasurement.process.EpochPacket
 */
public abstract class StepPacketUser extends PacketUser {

    public StepPacketUser() {
    }

    @Override
    public final void process(Packet p) {
        if (p instanceof EpochPacket) {
            doStep((EpochPacket) p);
        } else {
            process2(((edu.usc.enl.dynamicmeasurement.data.DataPacket) p));
        }
    }

    /**
     * It is privat to force any subclass to step and reset together
     *
     * @param p
     */
    private void doStep(EpochPacket p) {
        step(p);
        reset();
    }

    /**
     * Asynchronously step and reset
     *
     * @param p
     */
    public void forceStep(EpochPacket p) {
        doStep(p);
    }

    /**
     * Process a data packet
     *
     * @param p
     */
    protected abstract void process2(DataPacket p);

    /**
     * An epoch is finished.
     *
     * @param p
     */
    protected abstract void step(EpochPacket p);

    /**
     * Is called at the end of a step. Make sure you reset all the internal objects too
     */
    protected void reset() {

    }

}
